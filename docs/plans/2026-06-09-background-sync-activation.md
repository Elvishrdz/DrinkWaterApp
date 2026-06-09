# Plan: Background Auto-Sync Activation & Unified Sync Status

**Built on top of:** `docs/plans/2026-05-26-cloud-backup-feature.md`,
`docs/plans/2026-06-07-multiplatform-sync.md`
**Date:** 2026-06-09
**Status:** Planning

---

## Goal

The backup infrastructure (WorkManager worker, scheduler, connectivity observer, interval
preference) is ~80% built but the **periodic background sync is never activated**, and the four
worker-driven sync triggers (periodic, on-save, foreground, reconnect) are **invisible to the
Settings UI**. This plan:

1. Activates automatic background backup (daily at a user-chosen hour + connectivity-aware retry).
2. Prevents manual/background sync conflicts from confusing the UI.
3. Introduces a **single source of truth** for sync status so every trigger surfaces
   "Syncing…" and context-appropriate error messages in Settings.
4. Adds Settings UI to configure the daily backup hour.
5. Hardens the shared `Mutex` DI registration.

---

## Current state (verified)

| Concern                                                     | Status                            | Location                                                |
|-------------------------------------------------------------|-----------------------------------|---------------------------------------------------------|
| `CloudBackupWorker` (CoroutineWorker, retry logic)          | ✅ Built                           | `backup/.../data/worker/CloudBackupWorker.kt`           |
| `WorkManagerBackupScheduler` (periodic + one-time builders) | ✅ Built                           | `compose/.../core/backup/WorkManagerBackupScheduler.kt` |
| `ConnectivityObserver` (`Flow<Boolean>`)                    | ⚠️ Built, **never collected**     | `backup/.../data/observer/ConnectivityObserver.kt`      |
| `getBackupAutoIntervalDays()` pref                          | ⚠️ Exists, **never read/written** | `backup/.../domain/BackupPreferences.kt`                |
| `SyncCoordinator.onDataChanged()` (on-save debounce)        | ✅ Wired                           | `backup/.../data/SyncCoordinator.kt`                    |
| `Mutex` cross-path mutual exclusion                         | ✅ Works (shared singleton)        | `backup/.../di/BackupCoreModule.kt`                     |
| `scheduleAutoSync()`                                        | ❌ **Never called**                | —                                                       |
| `triggerForegroundSync()`                                   | ❌ **Never called**                | —                                                       |
| `_syncState` reflects worker syncs                          | ❌ Worker bypasses repository      | `backup/.../data/BackupRepositoryImpl.kt`               |

### Key findings

- **No periodic backup ever runs.** The only `BackupScheduler` call in app code is
  `cancelAll()` on disconnect (`SettingsViewModel.kt:448`). Nothing schedules the periodic worker.
- **All worker-driven syncs are invisible.** `CloudBackupWorker` calls `SyncBackupUseCase`
  directly, bypassing `BackupRepositoryImpl._syncState`, which the Settings screen observes
  (`SettingsViewModel.kt:211`). On-save, periodic, foreground, reconnect → never show "Syncing…".
- **False success on overlap.** Manual "Sync now" during a background sync: repo sets
  `InProgress`, `mutex.tryLock()` fails → `Success(AlreadyInProgress)` → repo maps *every*
  `Success` to `BackupSyncState.Success` → UI shows a checkmark though nothing synced.
- **Data integrity is already safe.** `Mutex` and `SyncBackupUseCase` are Koin singletons,
  shared by worker + repository, so overlapping syncs are mutually exclusive at the data layer.
  The problems are **UX-level**, not corruption.
- **Generic `Mutex` DI key.** `single { Mutex() }` is keyed by bare type; any future
  `get<Mutex>()` collides with the backup lock (latent footgun).

---

## Design: the backbone — `BackupSyncStatusHolder`

A process-wide singleton that **every** sync trigger writes to and the Settings screen observes.
Replaces the repository-local `_syncState` as the single source of truth.

```kotlin
// backup/domain/model/BackupSyncStatus.kt
enum class SyncTrigger { MANUAL, ON_SAVE, PERIODIC, FOREGROUND, RECONNECT, CONNECT }

sealed interface SyncDisposition {
    // A retry IS guaranteed → reassure the user it will happen automatically.
    data object WaitingForConnection :
        SyncDisposition  // offline / constraint unmet; flushes on reconnect
    data object WillRetry : SyncDisposition             // retryable, attempts remain (backoff)

    // No guaranteed retry → tell the user to use manual "Sync now".
    data object Failed : SyncDisposition                // non-retryable or attempts exhausted
}

data class BackupSyncStatus(
    val running: Boolean = false,
    val activeTrigger: SyncTrigger? = null,
    val lastOutcome: Outcome? = null,
) {
    sealed interface Outcome {
        data class Success(val syncedAt: Long, val trigger: SyncTrigger) : Outcome
        data class Failure(
            val error: BackupError,
            val trigger: SyncTrigger,
            val disposition: SyncDisposition,
        ) : Outcome
    }
}
```

```kotlin
// backup/data/BackupSyncStatusHolder.kt
class BackupSyncStatusHolder(private val prefs: BackupPreferences) {
    private val _status = MutableStateFlow(BackupSyncStatus())
    val status: StateFlow<BackupSyncStatus> = _status.asStateFlow()

    fun onStarted(trigger: SyncTrigger) { /* running=true, activeTrigger=trigger */
    }
    suspend fun onSuccess(trigger: SyncTrigger, at: Long) { /* + persist clear last error */
    }
    suspend fun onFailure(trigger, error, disposition) { /* + persist last error+disposition */
    }
}
```

- Lives in the same process as the worker (WorkManager default), so the in-memory `StateFlow`
  is shared across worker + UI.
- **Persists** last failure (`trigger` + `disposition`) via `BackupPreferences` (extend the
  existing `setBackupLastError`) so Settings still shows the last outcome after process death.

### Settings status mapping `(trigger, disposition) → copy`

**Guiding rule (per user decision):** if a retry is *guaranteed*, reassure the user it will
happen automatically; if a retry *cannot be ensured* (`Failed`), tell them to use manual "Sync now".

| Situation                                      | Message (string resource)                                                  |
|------------------------------------------------|----------------------------------------------------------------------------|
| `running == true`                              | "Syncing…" + all backup actions disabled                                   |
| `ON_SAVE` + `WillRetry`/`WaitingForConnection` | "Couldn't save your last change to backup. We'll try again automatically." |
| background + `WaitingForConnection`            | "Auto-backup failed. Will retry when the connection is restored."          |
| background + `WillRetry`                       | "Auto-backup failed. Will retry shortly."                                  |
| any + `Failed`                                 | "Auto-backup failed. Tap Sync now to retry." (+ inline error row)          |
| `Success`                                      | "Backed up <relative time>"                                                |

The "waiting for connection" case frequently occurs **before** the worker runs (WorkManager
won't start it while the `NetworkType.CONNECTED` constraint is unmet). So the Settings state is a
`combine` of: `holder.status` + `syncPending` pref + `ConnectivityObserver.isConnected`. When
`syncPending && !isConnected`, show the waiting message even though no `Outcome` was published.

---

## Work breakdown

### Phase 0 — Mutex hardening (prerequisite, isolated)

- Add `class BackupSyncLock { val mutex = Mutex() }`.
- DI: replace `single { Mutex() }` with `singleOf(::BackupSyncLock)`.
- Update `SyncBackupUseCase` and `MigrateBackupUseCase` to inject `BackupSyncLock` and use
  `lock.mutex`. (They keep sharing one instance — correct: sync & migrate stay mutually exclusive.)
- Update fakes/tests accordingly.

### Phase 1 — `BackupSyncStatusHolder` + funnel all triggers through it

- Add the status model + holder (above). Register as `single` in `backupCoreModule`.
- **Repository:** delete `_syncState`; delegate `syncState` to `holder.status` (mapped to the
  existing `BackupSyncState` for backward compat, or migrate callers to the richer model).
  Fix the false-success: map `SyncResult.AlreadyInProgress` so it does **not** render as Success.
- **Worker:** route through the holder. The worker reads its `SyncTrigger` from `inputData`
  (added by the scheduler per trigger), calls `holder.onStarted(...)` before sync and
  `onSuccess/onFailure(...)` after, deriving `SyncDisposition` from its existing
  `runAttemptCount`/error branch. Keep returning `Result.retry()`/`failure()` for WorkManager.
- **Scheduler:** attach `workDataOf(KEY_TRIGGER to trigger.name)` to every request so the worker
  knows its source.

### Phase 2 — Activate daily auto-sync at a chosen hour

**Decision:** schedule by **hour-of-day** (e.g. "back up daily at 02:00"), not a raw interval.
WorkManager's `PeriodicWorkRequest` cannot target a clock time, so use the
**self-rescheduling one-time work** pattern:

- Replace `BackupScheduler.scheduleAutoSync(intervalDays)` with
  `scheduleDailyAutoSync(hourOfDay: Int)`. It enqueues a `OneTimeWorkRequest` with
  `setInitialDelay(...)` computed as the ms until the next occurrence of `hourOfDay`, the
  `NetworkType.CONNECTED` constraint, and `ExistingWorkPolicy.UPDATE` under
  `WORK_NAME_PERIODIC`. The trigger tag is `PERIODIC`.
- On completion, the worker **re-enqueues itself** for the next day (calls
  `scheduler.scheduleDailyAutoSync(hour)` at the end of `doWork()`, reading the persisted hour).
  This keeps the daily cadence without `PeriodicWorkRequest`.
- Replace the `getBackupAutoIntervalDays()` pref with `getBackupAutoHour()/setBackupAutoHour()`
  (default e.g. 2 = 02:00). On-save keeps data fresh between daily runs, so a coarse daily cadence
  is acceptable (per user note).
- Schedule on **successful connect** and on app start for an already-connected user via a
  `BackupAutoSyncInitializer : AppInitializer` (registered like `AppLifecycleObserver`) that, if
  backup is enabled, calls `scheduleDailyAutoSync(prefs.getBackupAutoHour())`.
- Note: WorkManager is inexact (Doze/batching) — the run may drift from the exact hour. Acceptable.

### Phase 3 — Connectivity-aware retry ("postpone until internet")

- Add a `BackupConnectivityInitializer : AppInitializer` that collects
  `ConnectivityObserver.isConnected`; on `false → true` **and** `prefs.getSyncPending()`, call
  `scheduler.triggerOnChangeSync()`. (WorkManager's constraint already covers in-flight jobs;
  this covers the app-foregrounded-while-offline case and drives the "waiting" → "retrying" UI.)
- Wire `triggerForegroundSync()` into `AppLifecycleObserver.onStart` (guarded by backup enabled).

### Phase 4 — Settings UI

- **Hour-of-day selector.** Add `autoBackupHour` to `BackupUiState`; add a `BackupRow` with a
  time/hour picker ("Back up daily at HH:00"). On change: persist via `setBackupAutoHour` and
  call `scheduleDailyAutoSync(hour)` to reschedule (`ExistingWorkPolicy.UPDATE`).
- **Status + errors.** Drive the provider-row status text and a new error row from the
  `combine` described above. Disable Sync now / Restore / **Disconnect** / interval picker while
  `running`. (Disconnect is currently un-gated.)
- New string resources for each message in the mapping table.

### Phase 5 — Tests

- `BackupSyncStatusHolder` transitions (start → success/failure, disposition derivation).
- Worker publishes correct status per trigger + disposition (extend existing
  `SyncBackupUseCaseTest`/worker coverage with `FakeBackupPreferences`).
- Repository `AlreadyInProgress` no longer maps to Success.
- Connectivity initializer triggers sync on reconnect when `syncPending`.
- `FakeBackupScheduler` records `scheduleDailyAutoSync` hour; worker re-enqueues next day.
- Settings VM state mapping for each `(trigger, disposition)` combination.

---

## Edge cases & considerations

- **Process death mid-sync.** Holder `running` is in-memory; the worker's `try/finally` publishes
  a terminal state in normal flow. On cold start the holder defaults to `running=false` and
  Settings reconciles via persisted last outcome — acceptable. (Optional hardening: cross-check
  `WorkManager.getWorkInfos` RUNNING state; deferred unless flakiness appears.)
- **`InitialSyncPending` guard stays.** `SyncBackupUseCase` already skips automatic merges while a
  first-connection decision is owed — background activation must not bypass this.

- **Retry messaging (per user decision).** When a retry is guaranteed (`WillRetry` /
  `WaitingForConnection`), show reassuring copy ("we'll try again automatically" / "…when the
  connection is restored"). When a retry **cannot** be ensured (`Failed`), don't claim a precise
  ETA — instead direct the user to manual "Sync now". WorkManager controls the exact backoff delay,
  so timed copy stays vague ("shortly").

- **Hour-of-day accuracy.** WorkManager is inexact by design (Doze/batching) — the daily run may
  drift from the chosen hour. The self-rescheduling one-time-work pattern + on-save keep data
  fresh regardless.

---

## Decisions (resolved)

1. **Schedule granularity:** ✅ **Hour-of-day** — daily backup at a user-chosen hour via
   self-rescheduling one-time work. On-save keeps the backup current between daily runs.
2. **Offline handling:** ✅ **WorkManager constraints + reconnect flush** — the
   `NetworkType.CONNECTED` constraint plus a `ConnectivityObserver`-driven flush when
   connectivity returns (Phase 3).
3. **Retry messaging:** ✅ Guaranteed retry → reassure automatically; no guaranteed retry
   (`Failed`) → prompt the user to use manual "Sync now".

---

## Suggested build order

`Phase 0 (Mutex)` → `Phase 1 (holder backbone)` → `Phase 2 (activate periodic)` →
`Phase 3 (connectivity)` → `Phase 4 (UI)` → `Phase 5 (tests alongside each phase)`.

Phases 0 and 1 are the foundation; everything else layers on the holder.
