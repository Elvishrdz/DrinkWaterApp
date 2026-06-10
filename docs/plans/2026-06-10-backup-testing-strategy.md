# Plan: Backup Feature Testing Strategy

**Date:** 2026-06-10
**Status:** First pass implemented (2026-06-10) — Layer A scenarios 1–5, Layer B (Robolectric),
Layer C (SyncCoordinator). Decisions: Robolectric ✓, Layer C ✓, Layer A first pass = the 5
scenarios.

## Delivered in first pass

- **Layer A** — `InMemoryBackupDataSource` + `InMemoryCloudBackupProvider` test doubles (faithful
  envelope round-trip); `BackupSyncScenarioTest` covering the 5 scenarios end-to-end through the
  real `SyncBackupUseCase`/`RestoreBackupUseCase`/`MergeEngine`.
- **Layer B** — Robolectric added (`robolectric` 4.14.1 + `androidx.test:core-ktx` +
  `unitTests.isIncludeAndroidResources`); `BackupSerializerRoundTripTest` exercises the real
  Room-backed serializer (serialize→parse→write round-trip, tombstone survival, fingerprint match).
- **Layer C** — `SyncCoordinatorTest` for the on-save engine (debounce coalescing, change-tracker
  recording, enabled/disabled guards). **Note:** a full `SettingsViewModel` state test was
  intentionally skipped — its ~18 constructor deps + ~15 combined flows make it brittle to non-UI
  VM refactors, contradicting the resilience goal. If specific status/schedule mapping coverage is
  wanted, extract those pure mappers from the VM and unit-test them (follow-up).

## Remaining (follow-up passes)

Layer A scenarios 6–15 (no-op skip, initial-connection decision, corruption, migration, failure
paths, concurrency, tombstone-purge ordering, bounded follow-up, schedule delay math,
`currentBackupSchedule` mapping).

---

## Goal

Shield the cloud-backup feature with a durable test suite that (a) verifies each real-world
scenario works, and (b) does **not** break when the UI is redesigned. Tests must catch regressions
in sync, merge, restore, and scheduling logic for future changes.

---

## Guiding principle: test below the Composables

A backup is a **data** operation, not a UI one. The Composable layer is the only thing that changes
in a visual redesign, so we test **everything underneath it** and nothing in it:

```
Composables (NOT tested — change freely)         ← redesign-proof boundary
  └─ ViewModel state mapping  (Layer C, optional)
       └─ Repository / Use cases / MergeEngine    (Layer A — the core)
            └─ BackupDataSource (Room) + CloudProvider (network)  (Layer B)
```

Assert **state and data**, never widgets. A redesign that keeps the ViewModel contract leaves every
test green.

---

## Current coverage (what already exists)

`backup` module, JUnit4 + kotlinx-coroutines-test + hand-written fakes (no mockk-heavy, no
Robolectric):

- `MergeEngineTest` — **strong**: unions, last-writer-wins, tombstone rules 1–4, goal/prefs conflict
  windows. (14 cases)
- `SyncBackupUseCaseTest` — **thin**: first upload, hash-skip, lock/AlreadyInProgress, error
  persist.
  Does **not** exercise download→merge→upload (the fake's `parseRemoteData` returns null).
- `BackupRepositoryImplTest`, `RestoreBackupUseCaseTest`, `BackupSyncStatusHolderTest`,
  `BackupMigrationManagerTest`, `BackupContentFingerprintTest`, `CheckBackupStatusUseCaseTest`,
  `DisconnectBackupUseCaseTest`.

**Gaps:** end-to-end sync scenarios with real local+remote data, restore-overwrite semantics,
serialization round-trip against real Room, scheduling, the bounded on-change follow-up, and the
ViewModel status/schedule mapping.

---

## Recommendation on test types

| Type                                 | Use it for                                                                         | Verdict                                                                                                                            |
|--------------------------------------|------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| **Unit (JVM, `test/`)**              | use cases, repository, merge, status holder, scheduler delay math, ViewModel state | **Primary.** Fast, no emulator, runs in `./gradlew test`, CI-friendly, UI-agnostic.                                                |
| **Robolectric (JVM, `test/`)**       | real `BackupSerializer` ↔ Room round-trip                                          | **Recommended** for the one place we need a real DB. Still `./gradlew test`, no device. Needs a new `robolectric` test dependency. |
| **Instrumentation (`androidTest/`)** | only if Robolectric can't host Room                                                | **Avoid** unless forced — needs an emulator, slow, flaky in CI.                                                                    |
| **Compose UI tests**                 | —                                                                                  | **Skip.** This is exactly what breaks on redesign.                                                                                 |

Net: **mostly JVM unit tests**, one thin Robolectric layer for DB fidelity, **zero** Compose UI
tests.

---

## Layer A — Sync engine scenario tests (core, no new deps)

`backup` module, pure JVM. Add two richer test doubles so the **real** `MergeEngine` +
`SyncBackupUseCase` run end-to-end:

- **`InMemoryBackupDataSource`** — holds real `BackupData` (history/goals/prefs lists);
  `serialize()`
  encodes it and `parseRemoteData()` decodes it through a self-consistent JSON shape;
  `writeBackupData()`
  mutates its state; `localContentFingerprint()` derived from content. Lets merges actually compute.
- **`InMemoryCloudBackupProvider`** — stores the uploaded string and returns it on `download()` (a
  real in-memory cloud), so upload→download round-trips. (Today's `FakeCloudBackupProvider` can't
  round-trip; keep it for simple cases, add this for scenarios.)

### Scenarios (maps to your list + the must-haves)

1. **Clean app, no logs, no remote** → first sync uploads an empty data envelope; `Uploaded`;
   provider received one upload; hash + lastSyncAt persisted.
2. **App has logs, no remote** → upload contains exactly those history/goal records; `Uploaded`.
3. **Logs exist AND remote backup exists** → download + merge + upload; merged result is the union;
   `writeBackupData` applied locally; re-upload happens; seen-devices updated.
4. **Local has deleted records (tombstones), remote still has them live** → after merge the records
   stay deleted (tombstone within retention wins, no resurrection); end-to-end through
   `SyncBackupUseCase`, not just `MergeEngine`.
5. **Restore overrides local** → `RestoreBackupUseCase` replaces local entirely with remote
   (not a merge); pre-existing local-only records are gone afterward.
6. **No changes since last sync** → identical content → `SkippedNoChanges`, **no** upload.
7. **First-connection decision** (`InitialConnectionSyncUseCase`): keep-remote / upload-local /
   merge each produce the right local + remote end state.
8. **Corrupted / unreadable remote** → recovery decision path (overwrite vs leave).
9. **Schema migration** — older-schema remote is migrated before merge (extend existing).
10. **Failure paths** — upload/download failure → error persisted, correct `BackupResult.Failure`;
    no partial local writes.
11. **Concurrency** — `AlreadyInProgress` when the lock is held (have it; assert no upload).
12. **Tombstone purge** runs only **after** a successful upload.

### Worker / scheduling logic (backup module, JVM)

13. **Bounded on-change follow-up** — `BackupChangeTracker` + worker re-enqueue: a change during a
    run enqueues exactly one follow-up; no change → none. (Tests the cap you asked about.)
14. **Schedule delay math** — `WorkManagerBackupScheduler` initial-delay for `Daily(hour)` vs
    `EveryHours(n)`; daily picks the next future occurrence. (Extract the delay calc to a pure
    function so it's unit-testable without WorkManager.)
15. **`currentBackupSchedule()`** maps prefs (mode + hour/interval) → correct `BackupSchedule`.

---

## Layer B — Serialization + Room round-trip (Robolectric)

`compose` module, `test/` with Robolectric. Verifies the real wire format and Room mapping that
Layer A's in-memory double can't:

- Insert real logs + goals via DAO into an in-memory `AppDatabase` → `BackupSerializer.serialize()`
  → `parseRemoteData()` → `writeBackupData()` → assert the DB matches (round-trip fidelity).
- Field mapping: timestamps, device id, `deletedAt` tombstones, goals (epoch-day), preferences.
- `localContentFingerprint()` == `remoteContentFingerprint()` for identical content; differs when
  content differs (drives the hash-skip in scenario 6).
- Backwards-compat: a fixed older-schema JSON fixture deserializes correctly post-migration.

**Dependency:** add `robolectric` to the version catalog + `compose` `testImplementation`. If you'd
rather not add Robolectric, this layer becomes `androidTest/` instrumentation instead (slower, needs
an emulator) — or we drop it and rely on Layer A's in-memory round-trip only (less DB fidelity).

---

## Layer C — ViewModel state mapping (optional, redesign-proof)

`compose` module, JVM (koin-test + lifecycle-runtime-testing already present). Asserts the state the
UI *would* render, never the UI itself:

- `SettingsViewModel`: `isSyncing`, error/status-message mapping per `(trigger, disposition)`,
  `lastSyncAt` updates on success, `nextSyncAt` from WorkInfo, schedule change persists +
  reschedules,
  actions gated while syncing.
- `BackupDevViewModel`: status label + work-info summary + notification-mode toggle.

These cover "UI logic" without being UI tests — a Composable redesign won't touch them.

---

## What this buys you

- Every scenario you listed (1–5) is covered end-to-end, plus the silent failure modes that bite in
  production (corruption, migration, concurrency, no-op skip, tombstone purge).
- ~90% of the suite is plain JVM unit tests → fast, deterministic, CI-friendly.
- The whole suite lives **below** the Composables, so UI redesigns don't force test changes.

---

## Open decisions (need your call before I build)

1. **Layer B (Robolectric Room round-trip):** add the `robolectric` dependency (recommended), use
   `androidTest` instrumentation instead, or skip and rely on Layer A's in-memory round-trip?
2. **Layer C (ViewModel tests):** include now, or defer?
3. **Scope of first pass:** all of Layer A now, or a focused subset (your 5 scenarios) first then
   expand?

---

## Suggested order

Layer A doubles → scenarios 1–6 → 7–12 → worker/schedule 13–15 → (Layer B) → (Layer C).
