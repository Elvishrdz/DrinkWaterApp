# Tombstone & Purge Guide (Deletion Sync)

How record deletions propagate across devices via the cloud backup, why we keep
"tombstones", and the rules the purge must follow so deletions are never lost and deleted
records never come back as ghosts.

> Status: **implemented**. This documents the live design;
> see [§9](#9-current-state-vs-target--implemented)
> for the implementation map and the one remaining inherent limit.

---

## 1. Why tombstones exist

The cloud backup is a single JSON file reconciled with a **last‑writer‑wins merge** per
record (`MergeEngine`). Records are keyed by `id` (history) / `effective_date` (goals).

If a user deletes a drink log and we **physically** remove the row, the next merge sees the
record present on the cloud (and on other devices) but missing locally — and the merge's
"record exists remotely but not locally" branch treats it as **new** and re‑adds it. The
deletion silently bounces back.

To communicate "this record was deleted" rather than "this record is absent", we keep the
row as a **tombstone**: the record stays in the DB with `deleted_at` set to the deletion
time (and `updated_at` bumped so it wins conflict resolution). Tombstones are:

- **Hidden** from all UI / stats / streak queries (`WHERE deleted_at IS NULL`).
- **Included** in the backup (`getAll()` returns them) so the deletion is uploaded.
- **Applied** by the merge: a tombstone with a newer `updated_at` beats a live copy.

A tombstone is a temporary marker. Once every device has seen it, it has done its job and
should be physically removed — that removal is the **purge**.

---

## 2. The lifecycle of a deletion

```
User deletes log X on Device A
        │
        ▼
softDelete(X): deleted_at = T, updated_at = T      (row kept, hidden from UI)
        │  syncCoordinator.onDataChanged()
        ▼
Sync: upload → cloud file now carries X as a tombstone
        │
        ▼
Other devices sync → merge sees tombstone (newer) → they delete their copy of X
        │
        ▼
… retention window passes (all devices have had time to sync) …
        │
        ▼
Purge: every device physically removes X, and the cloud sheds it on next upload
```

The two hard requirements:

1. **A tombstone must be uploaded before it is purged.** Otherwise the deletion is lost.
2. **A tombstone must outlive the slowest device.** Otherwise a long‑offline device that
   still holds the live record will resurrect it.

---

## 3. The retention window

`TombstonePolicy.retentionDays` (target: **60 days**, see §7) defines how long a tombstone
lives before it is eligible for purge.

```
expiryThreshold  = now − retentionMs        // tombstones older than this may be purged
warningThreshold = now − (retentionMs − 3d) // within 3 days of expiry → "near expiry"
```

Retention is the **only** protection against resurrection in a last‑writer‑wins file model:
a device offline **longer** than retention, still holding the live record, *will*
re‑introduce it once the tombstone is gone. You cannot fully eliminate this — you can only
size the window above the maximum realistic offline period. 60 days covers "phone in a
drawer for a month" comfortably; it does not cover "reinstalled after a year".

---

## 4. The purge rules (the important part)

Purge physically deletes tombstones from the **local** DB. It must obey **all** of:

### Rule 1 — Only purge tombstones that have been synced

A tombstone may be purged only once we are certain it reached the cloud. Concretely, purge a
tombstone `t` only if:

```
t.deleted_at ≤ expiryThreshold          // old enough
AND  !syncPending                        // no local changes waiting to upload
AND  lastRemoteSyncAt ≥ t.deleted_at     // a successful upload happened AFTER the deletion
```

The last two conditions guarantee the deletion was actually pushed. Without them, an offline
device that deleted a record 60+ days ago (but never connected) would purge the tombstone on
its **first** sync — *before* uploading it — losing the deletion and resurrecting the record.

### Rule 2 — Purge runs AFTER a successful sync, never before

Order within a sync session:

```
download → merge → write local → upload (success) → THEN purge
```

Purging before the upload is unsafe (Rule 1 violation in disguise). Purge at the end, only
when the session uploaded successfully, so the current device's tombstones are guaranteed to
be in the cloud copy we just wrote.

> Implemented: `SyncBackupUseCase` calls `purgeTombstonesUseCase()` right after a successful
> upload (both the merge path and the `uploadLocal` first‑sync path).

### Rule 3 — The merge must be retention‑aware

When the merge encounters a record that is **remote‑only** (`localRecord == null`) and that
record is a tombstone with `deleted_at < expiryThreshold`, it must **skip** it (not re‑add
it). Otherwise:

- Device A purges X locally.
- Next sync downloads the cloud (still has X tombstone) → merge re‑adds X → purge undone.
- The cloud never sheds the tombstone; it grows forever.

With this rule, once a tombstone is past retention, every device independently stops
propagating it, the next upload from any purged device writes a file **without** it, and the
cloud finally sheds it.

### Rule 4 — Tombstone wins over a re‑introduced live record (within retention)

If the merge sees a remote **live** record whose `id` matches a tombstone we still hold
locally, the **tombstone wins** regardless of `updated_at`, until the tombstone itself
expires. This stops a stale device's live copy from beating a valid deletion during the
retention window.

---

## 5. Walkthroughs

### A. Normal deletion (everyone online within retention) ✅

A deletes X → uploads tombstone → B/C sync within 60 days → they delete X → after 60 days all
purge → cloud sheds X. Clean.

### B. Device offline 16 days, then connects (retention = 60d) ✅

B still holds X **live**; the cloud still has X's tombstone (16 < 60). Merge: remote tombstone
`updated_at` (deletion time) > B's live `updated_at` (creation time) → tombstone wins → X
deleted on B. No resurrection. (Purge plays no role here — B's X is live, not a tombstone.)

### C. Device offline LONGER than retention ⚠️ (inherent limit)

B offline 70 days, cloud already shed X (all online devices purged it). B re‑uploads live X →
others re‑add it → resurrection. Mitigation: size retention above realistic offline periods;
optionally surface the near‑expiry warning. Not fully solvable in this model.

### D. Offline device with its OWN unsynced old deletion ✅ (fixed by Rule 1 & 2)

B deleted Y 65 days ago while offline, never synced. B connects:

- **Wrong (current):** purge runs first → Y (65d) purged before upload → cloud still has live
  Y → merge re‑adds Y → deletion lost, Y resurrects.
- **Right (target):** purge runs after upload **and** only when `lastRemoteSyncAt ≥ Y.deleted_at`.
  On this first sync, Y's tombstone uploads first; it is **not** purged this session (it was
  just synced now, the "old enough + already synced before" condition isn't met until a later
  session). Deletion preserved.

---

## 6. The near‑expiry warning

`PurgeTombstonesUseCase` counts tombstones in `[warningThreshold, expiryThreshold)` that are
**not yet synced** (`syncPending == true`) and sets `KEY_TOMBSTONE_WARNING_PENDING`. The UI
should surface this as "You have deletions that haven't backed up — connect to sync before
they expire." This is the user‑facing safety net for case D. (Flag exists; **not currently
consumed by any UI**.)

---

## 7. Tuning

| Knob                     | Where                                         | Target                  |
|--------------------------|-----------------------------------------------|-------------------------|
| Retention                | `TombstonePolicy.retentionDays`               | **60** (was 15)         |
| Near‑expiry warning lead | `PurgeTombstonesUseCase.WARNING_THRESHOLD_MS` | 3 days                  |
| Purge trigger            | `SyncBackupUseCase`                           | after successful upload |

---

## 8. Non‑backup users

Tombstones are a **sync artifact** — they only exist to communicate a deletion to other
devices / the cloud. A user who never enables backup has nothing to reconcile with, so the
whole tombstone + retention + purge machinery is irrelevant to them and must not leak.

**Rule: deletion behavior is conditional on whether backup is enabled.**

| Backup state | Delete action                                                   | Purge                     |
|--------------|-----------------------------------------------------------------|---------------------------|
| **Enabled**  | `softDelete` (write tombstone) → syncs → purged after retention | via sync (§4)             |
| **Disabled** | `dao.delete` (physical removal immediately)                     | n/a — nothing accumulates |

The delete path checks `backupPreferences.getBackupEnabled()` (already available, suspend
read in the IO block) and branches.

**Transition — turning backup OFF:** physically purge any remaining tombstones during
disconnect/disable. They're hidden from the UI and have no further local purpose, so leaving
them would just be dead rows. (`DisconnectBackupUseCase` → purge‑all‑tombstones on the data
source.)

**Edge case — disable → delete → re‑enable (same account):** while backup was off, deletes
were physical with no tombstone, but the cloud still holds those records as live. On
re‑connect, a **Merge** could resurrect them. This is already user‑mediated by the
first‑connection dialog: choosing **Upload local (replace backup)** makes the local
deletions win. No extra machinery needed — just be aware of it.

Backup‑off deletes are now hard deletes, and `DisconnectBackupUseCase` purges all remaining
tombstones (`purgeExpiredTombstones(Long.MAX_VALUE)`), so non‑backup users never accumulate
hidden rows.

---

## 9. Current state vs. target — ✅ IMPLEMENTED

All target rules below are implemented as of this change.

| Behavior                                        | State                                             |
|-------------------------------------------------|---------------------------------------------------|
| Soft delete writes a tombstone (backup on)      | ✅                                                 |
| Tombstones hidden from UI/stats/streaks         | ✅                                                 |
| Tombstone uploaded & applied by merge           | ✅                                                 |
| Purge timing — after successful upload (Rule 2) | ✅ `SyncBackupUseCase` (merge + `uploadLocal`)     |
| Purge only when synced (Rule 1)                 | ✅ `!syncPending && deleted_at ≤ lastRemoteSyncAt` |
| Merge skips expired remote tombstones (Rule 3)  | ✅ `MergeEngine` history + goals                   |
| Tombstone wins over re‑introduced live (Rule 4) | ✅ `MergeEngine`, within retention                 |
| Retention window                                | ✅ 60 days (`TombstonePolicy`)                     |
| Near‑expiry warning consumed by UI              | ✅ `TombstoneSyncWarningDialog` (pre‑existing)     |
| Delete conditional on backup state (§8)         | ✅ hard delete when backup off (both history VMs)  |
| Purge leftover tombstones on disable (§8)       | ✅ `DisconnectBackupUseCase` purges all            |

### Key implementation points

- `MergeEngine.merge(local, remote, deviceId, expiryThreshold)` — new `expiryThreshold` arg
  (= `now − retentionMs`, computed in `SyncBackupUseCase`).
- `PurgeTombstonesUseCase` caps the purge cutoff at `min(expiryThreshold, lastRemoteSyncAt)`
  and skips entirely while `syncPending`.
- Purge moved from start‑of‑sync to **after** each successful upload.
- History `onDelete` (both `HistoryViewModel` and `HistoryV2ViewModel`) branches on
  `backupPreferences.getBackupEnabled()`.

### Tests

`MergeEngineTest` covers Rule 3 (expired remote‑only tombstone skipped / within‑retention
applied) and Rule 4 (local tombstone beats re‑introduced live; expired tombstone falls
through). Existing sync/disconnect tests updated for the new constructor params.

### Remaining inherent limit (not a bug)

Case C (device offline **longer** than 60 days, still holding a live copy of a deleted
record) can still resurrect it — unavoidable in a last‑writer‑wins file model; only widened
by the retention window.
