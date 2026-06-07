# Plan: Multiplatform Sync (cross-device data sync via cloud)

**Built on top of:** `docs/plans/2026-05-26-cloud-backup-feature.md`
**Date:** 2026-06-07
**Status:** Planning

---

## Goal

When a user has multiple Android devices logged into the same Google (or Dropbox) account,
DrinkWaterApp should keep all of them in sync. Data written on Device A should eventually appear on
Device B without manual intervention and without data loss.

This is a pure extension of the existing backup feature — no new cloud provider, no new auth flows.
The transport layer (Google Drive / Dropbox) stays the same. What changes is the **merge strategy**
and the **trigger model**.

---

## Core problem with the current approach

The current `SyncBackupUseCase` does: serialize local DB → compare hash → upload. This is a "last
writer wins" strategy. Device A uploads at 09:00, Device B uploads at 09:05 (overwriting A's
upload), Device A syncs again at 09:10 and overwrites B. Devices never see each other's records —
they keep overwriting.

We need to change from **replace** to **merge**.

---

## Mental model: how sync works

```
Device A                    Cloud File                  Device B
─────────                   ──────────                  ────────
[local DB]  ──upload──>  [drinkwater_backup.json]  <──download──  [local DB]
            <──download──                           ──upload──>
```

The cloud file is the shared medium. Every sync operation is:

1. **Download** the remote file.
2. **Merge** remote records into local DB (add missing, resolve conflicts).
3. **Upload** the merged result.

This is a "sync" not just a "backup".

---

## Data model prerequisites

### 1. Every record needs a device origin

`BackupMetadata` already has a `deviceId` field (currently set to `""`). We need to populate it and
propagate it into each record.

Each drink history entry and daily goal already has a UUID (`id`). We need to add:

```json
{
  "id": "uuid",
  "drink_amount": 250.0,
  "status": "IN_APP",
  "timestamp": 1748300000000,
  "device_id": "device-uuid",
  "updated_at": 1748300000000
}
```

- `timestamp` = creation time (already exists, immutable).
- `updated_at` = last modification time on the originating device. For new records, equals
  `timestamp`. For edits (e.g., user corrects an entry), gets bumped.
- `device_id` = stable identifier for the device that created/last modified this record.

Preferences need a similar `updated_at`:

```json
"preferences": {
"unitPreference": "ML",
"updated_at": 1748300000000,
"device_id": "device-uuid"
}
```

### 2. Stable device identifier

A stable UUID stored in DataStore (written once on first launch, never changed). This is **not** the
Android `ANDROID_ID` — it's an app-scoped identifier so it survives Google account changes.

```kotlin
// Key in DataStore
KEY_DEVICE_ID: String  // UUID, generated once, persists forever
```

### 3. Schema version bump

Adding `device_id` and `updated_at` to records is a breaking schema change. Increment
`BackupMigrationManager.currentSchema` from 1 to 2 and register a migration that:

- Fills `device_id = ""` for all existing records (empty string = "unknown origin, treat as
  oldest").
- Fills `updated_at = timestamp` for history records, `updated_at = 0` for goals and prefs (safe:
  they'll lose conflict resolution to any real device data, which is the conservative choice).

---

## Merge algorithm

### Merge rules by data type

**Drink history (append-only by nature)**

Records are immutable once created in normal usage (user can delete but not edit amounts after
saving). The merge is a **union by UUID**:

```
merged_history = local_history ∪ remote_history
                 de-duplicated by id
                 conflict (same id, different data): take record with higher updated_at
```

**Daily goals**

Goals are keyed by `effective_date`. A date can have at most one active goal. Merge:

```
for each effective_date:
  local_goal  = local goals where effective_date matches
  remote_goal = remote goals where effective_date matches
  if only one side has it: take it
  if both have it and same goal_ml: no conflict
  if both have it and different goal_ml:
    → auto-resolve: take higher updated_at
    → if updated_at delta < 60 seconds: treat as true conflict → prompt user
```

The 60-second window catches the case where the user changed the goal on two devices "
simultaneously" (e.g., within the same session of setting up the app on both). Outside that window,
the newest change wins silently.

**Preferences**

Preferences are a single flat object. They do not have per-field versioning — the whole block has
one `updated_at`. Merge:

```
if local.preferences.updated_at >= remote.preferences.updated_at:
  keep local preferences (local is newer or same)
else:
  take remote preferences
  → if delta < 300 seconds: flag as soft conflict (show subtle notification)
```

A "soft conflict" for preferences means: "We updated your settings from another device." No blocking
dialog — just an informational banner in Settings for one session.

**Deletions (tombstones)**

Deletions are the hard problem. If Device A deletes a record and Device B hasn't synced yet, a naive
merge will resurrect the deleted record (it's missing from A's upload but present in B's local DB).

Solution: **soft deletes + tombstones**.

Add a `deleted_at: Long?` field to history and goal records. When the user deletes a record:

- Set `deleted_at = now()`, `updated_at = now()`.
- Do NOT remove from local DB immediately.
- The record is included in the backup with `deleted_at` set.
- On the receiving device, if the remote record has `deleted_at` and the local copy doesn't (or has
  an older `updated_at`): apply the deletion locally.
- Tombstones are physically purged from DB (and from future backups) after 15 days.

```kotlin
data class TombstonePolicy(
    val retentionDays: Int = 15,   // configurable without a release
)
```

A `PurgeTombstonesUseCase` runs at app startup (or alongside the periodic sync) to physically delete
records where `deleted_at != null && deleted_at < now() - retentionDays`.

---

## Sync triggers

### Trigger 1: On record save (reactive sync)

Every time the user saves a drink or modifies a goal, we want to push that change to cloud quickly —
but not on every keystroke. Strategy:

- After a successful local DB insert/update, emit a "dirty" signal.
- `SyncCoordinator` (new class) debounces this signal with a 30-second window.
- After 30 seconds of no new writes, it enqueues a one-time WorkManager job (
  `BACKUP_SYNC_WORK_ON_CHANGE`).
- Network constraint: `NetworkType.CONNECTED`. If offline, the job stays pending in WorkManager's
  queue and runs when network resumes.

```kotlin
class SyncCoordinator(
    private val scheduler: BackupScheduler,
    private val prefs: BackupPreferences,
) {
    fun onDataChanged() {
        // debounce + enqueue one-time sync
    }
}
```

The `SyncCoordinator` is injected into the repository layer via a listener pattern — the data
repositories (DrinkHistoryRepository, DailyGoalRepository) call `syncCoordinator.onDataChanged()`
after successful writes. No direct dependency on backup internals from the data layer —
`SyncCoordinator` is passed as an optional callback.

### Trigger 2: App foreground (passive check)

When the app comes to foreground (via `DefaultLifecycleObserver`), if:

- Backup is enabled.
- Last sync was > `min(autoIntervalDays × 24h, 2h)` ago.
- Device has network.

→ Enqueue a one-time sync (`BACKGROUND_FOREGROUND_SYNC`).

This catches the scenario: user used Device A all morning, opens Device B in the afternoon, Device B
pulls Device A's data.

### Trigger 3: Periodic (existing WorkManager)

Unchanged. The existing `BACKUP_SYNC_WORK_PERIODIC` runs at the user-configured interval. Now it
runs the full merge-sync instead of the upload-only sync.

### Trigger 4: Manual (existing Settings button)

Unchanged. "Sync now" button triggers an immediate merge-sync and shows the result.

### Trigger 5: Connectivity restored

`ConnectivityObserver` (new or reuse existing network monitoring) detects transition from offline to
online. If there are pending offline changes (see section below), it enqueues a one-time sync
immediately.

---

## Offline handling

The existing feature already stores data locally in Room. Nothing changes there. What's new:

### Pending change queue

When the user writes data offline, the `SyncCoordinator.onDataChanged()` call still fires, but
WorkManager can't run the job (no network). WorkManager holds the job until network is available —
no extra work needed here. WorkManager's constraint system handles this natively.

However, we need to track whether there are **local changes not yet reflected in cloud**. This is
done by:

1. `KEY_SYNC_PENDING: Boolean` — set to `true` whenever a write happens, set to `false` after a
   successful upload.
2. `SyncCoordinator` sets this flag on every `onDataChanged()` call.
3. After a successful merge-sync upload, `SyncBackupUseCase` clears this flag.
4. The Settings UI shows "Cambios pendientes de sincronizar" when this flag is `true` and
   `KEY_BACKUP_LAST_ERROR` is null.

### User-scheduled backup (offline to scheduled)

If the user has set their sync interval to "Solo manual" but wants to ensure their data is backed up
before, say, turning off the device, they can tap "Sync now" in Settings. If offline at that moment,
show: "Sin conexión. Se sincronizará automáticamente cuando vuelva la red." and set the pending
flag.

---

## New `SyncBackupUseCase` flow (replaces the current one)

```
1. tryLock(mutex) → if fails: return AlreadyInProgress
2. Emit BackupSyncState.InProgress
3. Serialize local DB → localContent: String
4. Download remote file
   → if FileNotFound: this is the first sync from this device
       upload localContent, persist hash + timestamp, return Uploaded
   → if NetworkUnavailable: return Err(NetworkUnavailable)
5. Parse remote JSON → remoteBackup: BackupFile
6. Check schema compatibility (existing logic):
   → if remoteSchema > currentSchema: return Err(IncompatibleVersion)
   → if remoteSchema < currentSchema: migrate remote data forward
7. Merge: MergeEngine.merge(local = localData, remote = remoteData) → MergeResult
8. If MergeResult has user-visible conflicts:
   → Emit BackupSyncState.ConflictDetected(conflicts)
   → Wait for user resolution (suspend until SyncConflictResolver provides answer)
9. Apply merged data to local DB (deserializeAndImport the merged result)
10. Serialize merged result → mergedContent: String
11. Calculate mergedHash = SHA-256(mergedContent).take(32)
12. If mergedHash == lastUploadedHash AND no remote changes applied:
    → Emit BackupSyncState.UpToDate, return SkippedNoChanges
13. Upload mergedContent
14. Persist mergedHash + now() as lastSyncAt
15. Clear KEY_SYNC_PENDING
16. Clear KEY_BACKUP_LAST_ERROR
17. Emit BackupSyncState.Success(syncedAt = now())
18. return Uploaded
```

---

## `MergeEngine`

New class in `:backup`:

```kotlin
class MergeEngine {
    fun merge(
        local: BackupData,
        remote: BackupData,
        localDeviceId: String,
    ): MergeResult
}

data class BackupData(
    val history: List<DrinkHistoryEntry>,
    val dailyGoals: List<DailyGoalEntry>,
    val preferences: PreferencesSnapshot,
)

data class MergeResult(
    val mergedData: BackupData,
    val appliedRemoteRecords: Int,
    val conflicts: List<SyncConflict>,
)

sealed interface SyncConflict {
    data class GoalConflict(
        val effectiveDate: Int,
        val localGoal: DailyGoalEntry,
        val remoteGoal: DailyGoalEntry,
    ) : SyncConflict

    data class PreferencesConflict(
        val localPrefs: PreferencesSnapshot,
        val remotePrefs: PreferencesSnapshot,
    ) : SyncConflict
}
```

`MergeEngine` is a pure Kotlin class with no Android dependencies — fully unit-testable. It has no
side effects: it receives two snapshots and returns a merged snapshot with any conflicts flagged.

---

## Conflict resolution UI

### Auto-resolved conflicts (no UI)

- Drink history records from different devices → silently merged.
- Same record on both devices, one is newer → take newer silently.
- Preferences: one device clearly newer → take newer, show brief informational banner.

### User-prompted conflicts

These appear as a **sync conflict bottom sheet** before the upload completes.

**Goal conflict sheet:**

```
╔══════════════════════════════════════════════════╗
║  Diferencia en tu meta del día                   ║
╠══════════════════════════════════════════════════╣
║  Dos dispositivos guardaron una meta diferente   ║
║  para hoy. ¿Cuál quieres usar?                   ║
║                                                  ║
║  [Este dispositivo]      2000 ml  (hace 2 min)   ║
║  [Otro dispositivo]      2500 ml  (hace 5 min)   ║
╠══════════════════════════════════════════════════╣
║  Aplicar a todos los dispositivos                ║
╚══════════════════════════════════════════════════╝
```

**Preferences conflict sheet** (shown only if both sides changed within 5 minutes):

```
╔══════════════════════════════════════════════════╗
║  Configuración actualizada desde otro dispositivo║
╠══════════════════════════════════════════════════╣
║  Tus ajustes fueron modificados desde otro       ║
║  dispositivo. ¿Cuáles prefieres usar?            ║
║                                                  ║
║  [Este dispositivo]   ML · 250ml · 07:00-22:00   ║
║  [Otro dispositivo]   OZ · 8oz  · 08:00-23:00   ║
╠══════════════════════════════════════════════════╣
║  Usar este dispositivo    Usar el otro           ║
╚══════════════════════════════════════════════════╝
```

Multiple conflicts are shown in sequence (one sheet per conflict). The user resolves each before the
upload proceeds.

### `SyncConflictResolver`

A `CompletableDeferred`-based bridge between the use case (which is suspended waiting for
resolution) and the UI (which shows the sheet and collects the user's choice).

```kotlin
class SyncConflictResolver {
    suspend fun resolveGoalConflict(conflict: GoalConflict): GoalResolution
    suspend fun resolvePreferencesConflict(conflict: PreferencesConflict): PrefsResolution
}

enum class GoalResolution { USE_LOCAL, USE_REMOTE }
enum class PrefsResolution { USE_LOCAL, USE_REMOTE }
```

`SyncConflictResolver` is a singleton (Koin `single`). The ViewModel observes it as a Flow and
shows/hides sheets accordingly.

---

## BackupSyncState additions

```kotlin
sealed interface BackupSyncState {
    data object Idle : BackupSyncState
    data object InProgress : BackupSyncState
    data object UpToDate : BackupSyncState
    data class Success(val syncedAt: Instant, val newRecords: Int) : BackupSyncState
    data class ConflictDetected(val conflicts: List<SyncConflict>) : BackupSyncState
    data class Error(val error: BackupError) : BackupSyncState
}
```

`ConflictDetected` is emitted by the use case and causes the UI to show the conflict sheet. Once
resolved, the use case resumes and emits `InProgress` again before uploading.

---

## New preferences keys

```
KEY_DEVICE_ID: String                // stable UUID, generated once on first install
KEY_SYNC_PENDING: Boolean            // true if local changes not yet uploaded
KEY_LAST_REMOTE_SYNC_AT: Long?       // timestamp of the most recent remote file we read (for foreground check)
KEY_SYNC_ON_SAVE_ENABLED: Boolean    // user toggle: sync immediately on record save (default: true)
KEY_SEEN_DEVICES: String             // JSON array of SeenDevice; updated on every successful download
KEY_TOMBSTONE_WARNING_PENDING: Boolean  // true when tombstones near expiry and not yet synced
```

---

## `BackupPreferences` interface additions

```kotlin
suspend fun getDeviceId(): String
suspend fun setDeviceId(id: String)
suspend fun getSyncPending(): Boolean
suspend fun setSyncPending(pending: Boolean)
suspend fun getLastRemoteSyncAt(): Long?
suspend fun setLastRemoteSyncAt(millis: Long)
suspend fun getSyncOnSaveEnabled(): Boolean
suspend fun setSyncOnSaveEnabled(enabled: Boolean)
suspend fun getSeenDevices(): List<SeenDevice>
suspend fun setSeenDevices(devices: List<SeenDevice>)
suspend fun getTombstoneWarningPending(): Boolean
suspend fun setTombstoneWarningPending(pending: Boolean)
```

---

## `BackupError` additions

```kotlin
sealed interface BackupError : DataError {
    // existing errors...
    data object MergeCorrupted : BackupError   // remote data can't be parsed for merge
    data class ConflictPending(val conflicts: List<SyncConflict>) :
        BackupError  // user must resolve
}
```

---

## Settings UI additions

**New section under Cloud Backup:**

```
┌─────────────────────────────────────────────┐
│  SINCRONIZACIÓN ENTRE DISPOSITIVOS          │
├─────────────────────────────────────────────┤
│  Sincronizar al guardar         [toggle ON] │
│  Los datos se envían a la nube              │
│  automáticamente al registrar              │
│  una bebida.                                │
├─────────────────────────────────────────────┤
│  Dispositivos vinculados                    │
│  · Pixel 8 (este dispositivo)               │
│  · Galaxy S24 (hace 2 horas)                │
│  · Tablet Pro (hace 3 días)                 │
└─────────────────────────────────────────────┘
```

**"Dispositivos vinculados"** tracks every device that has ever written to the shared cloud file.
Since the cloud file always carries the last-writing device's `deviceId` + `deviceModel` in
metadata, we accumulate a local registry of seen devices:

```kotlin
data class SeenDevice(
    val deviceId: String,
    val deviceModel: String,
    val lastSeenAt: Long,   // epoch ms of the most recent remote file written by this device
    val isCurrentDevice: Boolean,
)
```

`KEY_SEEN_DEVICES: String` — persisted as a compact JSON array in DataStore. Updated on every
successful download: if the remote file's `deviceId` is not in the list, add it; if it is, update
`lastSeenAt`. The current device is also added to the list on first sync. The list grows over time
and is never pruned automatically (devices that stop being used just show an old `lastSeenAt`).

The merge logic is device-count-agnostic — it operates on two snapshots (local vs. remote)
regardless
of how many devices have ever contributed. N devices work exactly the same as 2.

**Status banner (when sync pending):**

```
⟳ Cambios pendientes de sincronizar — Tap to sync now
```

Shown when `KEY_SYNC_PENDING == true` and `KEY_BACKUP_LAST_ERROR == null`. Tapping triggers a manual
sync.

---

## New classes and files

| Module     | File                                               | Description                                                                            |
|------------|----------------------------------------------------|----------------------------------------------------------------------------------------|
| `:backup`  | `MergeEngine.kt`                                   | Pure merge logic: union history, resolve goal/prefs conflicts, apply tombstones        |
| `:backup`  | `SyncConflict.kt`                                  | Sealed interface for conflict types (GoalConflict, PreferencesConflict)                |
| `:backup`  | `MergeResult.kt`                                   | Output of MergeEngine: merged data + conflict list + appliedRemoteRecords count        |
| `:backup`  | `SyncConflictResolver.kt`                          | Bridge between suspended use case and UI (CompletableDeferred-based)                   |
| `:backup`  | `SyncCoordinator.kt`                               | Debounced "data changed" signal → enqueues one-time WorkManager sync                   |
| `:backup`  | `PurgeTombstonesUseCase.kt`                        | Physically deletes records where deleted_at < 15 days ago; sets tombstone warning flag |
| `:backup`  | `ConnectivityObserver.kt`                          | Observes network state; notifies SyncCoordinator when connectivity is restored         |
| `:backup`  | `SeenDevice.kt`                                    | Data class: deviceId, deviceModel, lastSeenAt, isCurrentDevice                         |
| `:compose` | `feature/settings/backup/SyncConflictSheet.kt`     | Bottom sheet for goal/prefs conflicts; one conflict at a time                          |
| `:compose` | `feature/settings/backup/SyncPendingBanner.kt`     | Inline banner shown when sync is pending                                               |
| `:compose` | `feature/settings/backup/LinkedDevicesSection.kt`  | Shows all seen devices from KEY_SEEN_DEVICES registry; N-device-agnostic               |
| `:compose` | `feature/home/today/TombstoneSyncWarningDialog.kt` | Warning dialog on Today screen when tombstones risk expiring without sync              |

---

## Files to modify

| File                                         | Change                                                                                                    |
|----------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `BackupMetadata.kt`                          | `deviceId` already present; ensure it's populated from `KEY_DEVICE_ID` in `BackupSerializer`              |
| `BackupPreferences.kt`                       | Add 4 new key pairs (deviceId, syncPending, lastRemoteSyncAt, syncOnSaveEnabled)                          |
| `DataStorePrefsStorage.kt`                   | Implement new BackupPreferences keys                                                                      |
| `SyncBackupUseCase.kt`                       | Replace upload-only flow with download → merge → upload flow                                              |
| `BackupSyncState.kt`                         | Add `ConflictDetected` state                                                                              |
| `BackupError.kt`                             | Add `MergeCorrupted`, `ConflictPending`                                                                   |
| `BackupScheduler.kt`                         | Add `triggerOnChangeSyncDebounced()` and `triggerForegroundSync()` methods                                |
| `BackupCoreModule.kt`                        | Register MergeEngine, SyncConflictResolver, SyncCoordinator, PurgeTombstonesUseCase, ConnectivityObserver |
| `BackupMigrationManager.kt`                  | Add schema 1→2 migration: populate `device_id = ""` and `updated_at = timestamp` on all records           |
| `BackupSerializer.kt` (`:compose`)           | Include `device_id` and `updated_at` in serialized records; read them back on import                      |
| Room entity: `DrinkHistoryEntity`            | Add `deviceId: String`, `updatedAt: Long`, `deletedAt: Long?` columns + Room migration                    |
| Room entity: `DailyGoalEntity`               | Add `deviceId: String`, `updatedAt: Long`, `deletedAt: Long?` columns + Room migration                    |
| Room entity: `PreferencesEntity` (if exists) | Add `deviceId: String`, `updatedAt: Long` or store in DataStore as `updated_at` alongside prefs           |
| `DrinkHistoryRepository` / `DailyGoalRepo`   | Call `syncCoordinator.onDataChanged()` after successful writes                                            |
| `BackupSettingsSection.kt`                   | Add sync-on-save toggle, pending banner, linked devices section                                           |
| `SettingsViewModel.kt`                       | Expose `isSyncPending: StateFlow<Boolean>`, handle `ConflictDetected` state → show conflict sheets        |
| `MainViewModel.kt` / `AppLifecycleObserver`  | On foreground: trigger foreground sync if conditions met                                                  |
| `DevBackupScreen.kt`                         | Add buttons: simulate multi-device conflict, simulate offline+sync, show MergeEngine output               |

---

## Schema changes (JSON format — schema version 2)

```json
{
  "metadata": {
    "schemaVersion": 2,
    "appVersionName": "2.1.0",
    "appVersionCode": 30,
    "savedAt": "2026-06-07T10:00:00Z",
    "platform": "android",
    "packageId": "com.eahm.drinkwaterapp",
    "provider": "google_drive",
    "userIdHash": "<SHA-256 del email>",
    "deviceId": "device-stable-uuid",
    "deviceModel": "Pixel 8",
    "locale": "es"
  },
  "data": {
    "history": [
      {
        "id": "uuid",
        "drink_amount": 250.0,
        "status": "IN_APP",
        "timestamp": 1748300000000,
        "device_id": "device-stable-uuid",
        "updated_at": 1748300000000,
        "deleted_at": null
      }
    ],
    "dailyGoals": [
      {
        "id": "uuid",
        "effective_date": 20000,
        "goal_ml": 2000.0,
        "device_id": "device-stable-uuid",
        "updated_at": 1748300000000,
        "deleted_at": null
      }
    ],
    "preferences": {
      "unitPreference": "ML",
      "timeFormat": "SYSTEM",
      "wakeHour": 7,
      "wakeMinute": 0,
      "sleepHour": 22,
      "sleepMinute": 0,
      "intervalHour": 0,
      "intervalMinute": 45,
      "amountPerDrink": 250.0,
      "device_id": "device-stable-uuid",
      "updated_at": 1748300000000
    }
  }
}
```

---

## Room migrations required

### Migration 1 → 2 (schema version bump)

Add to `DrinkHistoryEntity`:

```sql
ALTER TABLE drink_history ADD COLUMN device_id TEXT NOT NULL DEFAULT '';
ALTER TABLE drink_history ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0;
ALTER TABLE drink_history ADD COLUMN deleted_at INTEGER;
```

Add to `DailyGoalEntity`:

```sql
ALTER TABLE daily_goals ADD COLUMN device_id TEXT NOT NULL DEFAULT '';
ALTER TABLE daily_goals ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0;
ALTER TABLE daily_goals ADD COLUMN deleted_at INTEGER;
```

The `updated_at` default of `0` means: old records have no modification timestamp. When merging with
remote data, a remote record for the same UUID with any `updated_at > 0` wins over a local record
with `updated_at = 0`. This is the safe conservative choice — remote data (from another device)takes
precedence over old untracked local data.

---

## Implementation phases

### Phase 1 — Foundation (data model)

1. Generate and persist `KEY_DEVICE_ID` on first launch (`DeviceIdInitializer` that runs in
   `Application.onCreate` via Koin).
2. Add `device_id`, `updated_at`, `deleted_at` to Room entities. Write Room migration 1→2.
3. Update `BackupSerializer` to include these fields in serialized JSON and read them back.
4. Bump `BackupMigrationManager.currentSchema` to 2, register 1→2 migration for backup files.
5. Populate `deviceId` in `BackupMetadata` from `KEY_DEVICE_ID`.
6. Add new `BackupPreferences` keys and implement in `DataStorePrefsStorage`.

**Compile check:** `./gradlew :backup:compileDebugKotlin :compose:compileDebugKotlin`

### Phase 2 — MergeEngine (pure logic, fully tested)

1. Define `BackupData`, `SyncConflict`, `MergeResult` models.
2. Implement `MergeEngine.merge()` with all three merge rules (history, goals, prefs).
3. Implement `PurgeTombstonesUseCase`.
4. Write unit tests for `MergeEngine`:
    - History from two devices: union, no duplicates.
    - Same record from both sides: newer `updated_at` wins.
    - Deleted record on one side: tombstone applied on merge.
    - Goal conflict within 60-second window: flagged as `GoalConflict`.
    - Goal conflict outside 60-second window: auto-resolved to newer.
    - Preferences conflict within 5-minute window: flagged as `PreferencesConflict`.
    - Preferences conflict outside 5-minute window: auto-resolved to newer.

**Run:** `./gradlew :backup:test`

### Phase 3 — Sync triggers

1. Implement `SyncCoordinator` with debounce (30s, configurable for tests).
2. Implement `ConnectivityObserver` using `ConnectivityManager.NetworkCallback`.
3. Wire `SyncCoordinator.onDataChanged()` calls from `DrinkHistoryRepository` and
   `DailyGoalRepository`.
4. Add `triggerForegroundSync()` in `AppLifecycleObserver` / `MainViewModel`.
5. Add new WorkManager work name `BACKUP_SYNC_WORK_ON_CHANGE` to `BackupScheduler`.
6. Ensure all sync triggers respect `KEY_BACKUP_ENABLED == true` and `KEY_SYNC_ON_SAVE_ENABLED`.

### Phase 4 — Updated SyncBackupUseCase

1. Refactor `SyncBackupUseCase` to the new download → merge → upload flow.
2. Implement `SyncConflictResolver` (CompletableDeferred bridge).
3. Add `ConflictDetected` to `BackupSyncState`.
4. Wire `SyncConflictResolver` into the use case.
5. Update unit tests for `SyncBackupUseCase`:
    - First sync (no remote file): direct upload.
    - No changes since last sync: `SkippedNoChanges`.
    - Remote has new records: merge and upload.
    - Conflict: `ConflictDetected` emitted, deferred until resolver provides answer.
    - Offline: `NetworkUnavailable` returned, pending flag set.

### Phase 5 — Conflict resolution UI + tombstone warning

1. `SyncConflictSheet.kt` — shows goal or prefs conflict, collects user choice.
2. `SyncPendingBanner.kt` — inline banner for pending local changes.
3. Update `BackupSettingsSection.kt`: add sync-on-save toggle, pending banner, linked devices.
4. `LinkedDevicesSection.kt` — reads `KEY_SEEN_DEVICES` and renders all seen devices (N devices),
   marking the current device with "este dispositivo" and showing relative time for others.
5. Update `SettingsViewModel.kt` to observe `ConflictDetected` state and show sheets.
6. `TombstoneSyncWarningDialog.kt` — shown on Today screen when
   `KEY_TOMBSTONE_WARNING_PENDING == true`.
7. Update `TodayViewModel.kt` and `TodayScreen.kt` to show/dismiss the tombstone warning.
8. Add `KEY_SEEN_DEVICES` + `KEY_TOMBSTONE_WARNING_PENDING` to `BackupPreferences` and
   `DataStorePrefsStorage`.

### Phase 6 — Dev Playground additions

Add to `BackupDevScreen.kt`:

- **"Simular dos dispositivos"** — creates a fake remote file with records from a different
  `device_id`, then triggers a sync to exercise the merge path.
- **"Simular conflicto de meta"** — creates a remote goal for today with a different `goal_ml` and
  recent `updated_at` within 60 seconds.
- **"Simular preferencias en conflicto"** — creates remote prefs changed 2 minutes ago.
- **"Simular offline + sync"** — sets `KEY_SYNC_PENDING = true`, shows pending banner, then triggers
  sync.
- **"Simular tombstone gap warning"** — sets `KEY_TOMBSTONE_WARNING_PENDING = true` to preview the
  Today screen dialog without actually deleting records.
- **"Ver output del MergeEngine"** — runs merge between the current local snapshot and a
  configurable
  fake remote snapshot, then displays the full `MergeResult` in the scrollable log pane:
  ```
  MergeEngine output:
    appliedRemoteRecords: 3
    autoResolvedConflicts: 1
    userConflicts: 0
    tombstonesApplied: 0
    mergedHistory.size: 47
    mergedGoals.size: 12
  ```
  This log is **debug-only** and never surfaces in production UI.

### Phase 7 — Strings and translations

New strings to add:

```xml
<!-- Sync on save setting -->
<string name="backup_sync_on_save_title">Sincronizar al guardar</string><string
name="backup_sync_on_save_desc">Envía los datos a la nube al registrar una bebida
</string>

    <!-- Pending banner -->
<string name="backup_pending_banner">Cambios pendientes de sincronizar</string>

    <!-- Linked devices section -->
<string name="backup_linked_devices_title">Dispositivos vinculados</string><string
name="backup_this_device">este dispositivo
</string>

    <!-- Conflict sheet — goal -->
<string name="sync_conflict_goal_title">Diferencia en tu meta del día</string><string
name="sync_conflict_goal_body">Dos dispositivos guardaron una meta diferente. ¿Cuál quieres usar?
</string><string name="sync_conflict_this_device">Este dispositivo</string><string
name="sync_conflict_other_device">Otro dispositivo
</string><string name="sync_conflict_apply_all">Aplicar a todos los dispositivos</string>

    <!-- Conflict sheet — preferences -->
<string name="sync_conflict_prefs_title">Configuración actualizada desde otro dispositivo
</string><string name="sync_conflict_prefs_body">Tus ajustes fueron modificados desde otro
dispositivo. ¿Cuáles prefieres usar?
</string><string name="sync_conflict_use_local">Usar este dispositivo</string><string
name="sync_conflict_use_remote">Usar el otro
</string>

    <!-- Tombstone sync warning dialog (Today screen) -->
<string name="sync_tombstone_warning_title">Registros pendientes de sincronizar</string><string
name="sync_tombstone_warning_body">Tienes eliminaciones recientes que aún no se han
sincronizado con la nube. Conéctate a internet pronto para que los cambios se reflejen en todos
tus dispositivos. Si no sincronizas a tiempo, los registros eliminados podrían reaparecer desde
otro dispositivo.
</string><string name="sync_tombstone_warning_sync_now">Sincronizar ahora</string><string
name="sync_tombstone_warning_dismiss">Entendido
</string>

    <!-- Linked devices — relative time suffix -->
<string name="backup_device_last_seen">hace %1$s</string><!-- e.g. "hace 2 horas" -->
```

Note: `sync_merged_records` is **not** shown in any production UI. It is used only in the
`BackupDevScreen` debug log pane. Do not add it to production string resources; define it only
in the debug source set or inline in the dev composable.

Translate all production strings above to all 19 supported languages.

---

## Decisions (resolved)

| Decision                                  | Choice                                                                                                                                           |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| Race condition (two simultaneous uploads) | **Accept last-writer-wins at file level.** The merge on the next sync corrects any lost records. ETag check deferred to a future hardening pass. |
| Tombstone retention                       | **15 days.** Configurable via `TombstonePolicy(retentionDays = 15)` — change without a code release if needed.                                   |
| Show merge count to user                  | **No UI notification.** Add a dev-only log entry in `BackupDevScreen` log pane after every sync showing `MergeResult.appliedRemoteRecords`.      |
| "Sync on save" default                    | **Enabled by default.** User can turn it off in Settings.                                                                                        |
| Multi-device support (> 2 devices)        | **Show all devices in `LinkedDevicesSection`.** The merge logic is device-count-agnostic by design — N devices work the same as 2.               |

---

## What does NOT change

- Auth flows (Google Sign-In, Dropbox OAuth) — unchanged.
- Cloud file location (`drinkwater_backup.json` in app-private folder) — unchanged.
- `MigrateBackupUseCase` (provider migration) — unchanged; it copies bytes, the merge logic is
  irrelevant.
- `ConnectBackupUseCase` — unchanged in structure; the first sync now uses the new merge-aware
  `SyncBackupUseCase`.
- `RestoreBackupUseCase` — unchanged; explicit restore is a deliberate "replace local with remote"
  action, not a merge.
- WorkManager periodic job name and constraints — unchanged.
- The feature flag `FEATURE_CLOUD_BACKUP` gates the entire feature including sync.

---

## Risk assessment

| Risk                                                                          | Likelihood | Impact | Mitigation                                                                                                                                 |
|-------------------------------------------------------------------------------|------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------|
| Merge produces corrupted data on edge case                                    | Medium     | High   | MergeEngine is pure + unit-tested; all merge paths covered before shipping                                                                 |
| Race condition between two simultaneous uploads                               | Low        | Medium | ETag-based check deferred; merge on next sync recovers lost records                                                                        |
| Tombstone sync gap (device offline > 15 days, deleted record resurrected)     | Low        | Low    | Show `TombstoneSyncWarningDialog` on Today screen (see below)                                                                              |
| SyncConflictResolver deferred never resolved (UI dismissed without answering) | Low        | Medium | CompletableDeferred has a timeout; if not resolved in 60s, cancels the suspended coroutine and returns `AlreadyInProgress` on next attempt |
| Room migration failure on upgrade                                             | Low        | High   | Write migration test; always test on a device with existing data                                                                           |

### Tombstone sync gap — detailed handling

**Condition:** backup is enabled AND there are tombstone records whose `deleted_at` age is
approaching the 15-day purge window (threshold: < 3 days remaining before purge) AND the device has
not synced those tombstones yet (`KEY_SYNC_PENDING == true`).

**Behavior:**

When `PurgeTombstonesUseCase` detects this condition on startup or on periodic check, it emits a
flag `KEY_TOMBSTONE_WARNING_PENDING: Boolean = true`. The Today screen ViewModel reads this flag and
shows a non-blocking dialog:

```
╔══════════════════════════════════════════════════╗
║  ⚠ Registros pendientes de sincronizar           ║
╠══════════════════════════════════════════════════╣
║  Tienes eliminaciones recientes que aún no se    ║
║  han sincronizado con la nube. Conéctate a       ║
║  internet pronto para que los cambios se         ║
║  reflejen en todos tus dispositivos.             ║
║                                                  ║
║  Si no sincronizas a tiempo, los registros       ║
║  eliminados podrían reaparecer desde otro        ║
║  dispositivo.                                    ║
╠══════════════════════════════════════════════════╣
║  [Sincronizar ahora]          [Entendido]        ║
╚══════════════════════════════════════════════════╝
```

- "Sincronizar ahora" → triggers manual sync immediately (same as Settings button). If online, sync
  runs and clears the warning. If offline, shows "Sin conexión" snackbar and dialog stays
  dismissible.
- "Entendido" → dismisses the dialog for this session. The flag stays `true`; dialog will re-appear
  on next cold start until a sync clears it.
- The dialog is **only shown when `KEY_BACKUP_ENABLED == true`**. If the user has never enabled
  backup, deletions are local-only and tombstones are irrelevant — no dialog.

**If the user disables backup:**

- Local deletions are applied permanently. Tombstone records are physically deleted immediately on
  `DisconnectBackupUseCase` (no retention needed without sync).
- `KEY_TOMBSTONE_WARNING_PENDING` is cleared as part of `DisconnectBackupUseCase`.

**If the user re-enables backup after a gap:**

- `ConnectBackupUseCase` runs the merge-aware `SyncBackupUseCase` as its first sync.
- If the remote file was written by another device in the meantime, the full conflict resolution
  flow applies.
- The user decides: merge (conflicts resolved per our merge rules) or restore-from-cloud (replace
  local completely, losing any local-only changes including locally-deleted tombstones).

**New preference key:** `KEY_TOMBSTONE_WARNING_PENDING: Boolean`
**New file:** `:compose` → `feature/home/today/TombstoneSyncWarningDialog.kt`
**Modified files:** `PurgeTombstonesUseCase.kt` (emit warning flag), `TodayViewModel.kt` (read
flag + expose to UI), `TodayScreen.kt` (show dialog), `DisconnectBackupUseCase.kt` (clear flag on
disconnect).
