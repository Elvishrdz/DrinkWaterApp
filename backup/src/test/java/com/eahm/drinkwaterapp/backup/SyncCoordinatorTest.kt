package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupChangeTracker
import com.eahm.drinkwaterapp.backup.data.SyncCoordinator
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.FakeBackupScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests the on-save orchestration: debounce coalescing, change-tracker recording, and the
 * enabled/disabled guards. This is the engine behind "Sync on save" and is independent of any UI.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncCoordinatorTest {

    private val debounce = 1_000L

    private fun coordinator(
        scheduler: FakeBackupScheduler,
        prefs: FakeBackupPreferences,
        tracker: BackupChangeTracker,
        scope: kotlinx.coroutines.CoroutineScope,
    ) = SyncCoordinator(scheduler, prefs, tracker, debounceMs = debounce, scope = scope)

    private fun enabledPrefs() = FakeBackupPreferences().apply {
        backupEnabled = true
        syncOnSaveEnabled = true
    }

    @Test
    fun `rapid writes coalesce into a single enqueue`() = runTest {
        val scheduler = FakeBackupScheduler()
        val tracker = BackupChangeTracker()
        val coordinator = coordinator(scheduler, enabledPrefs(), tracker, backgroundScope)

        coordinator.onDataChanged()
        coordinator.onDataChanged()
        coordinator.onDataChanged()
        runCurrent() // let the three writes process before the debounce window elapses

        advanceTimeBy((debounce + 100).milliseconds)
        runCurrent()

        assertEquals(1, scheduler.triggerOnChangeSyncCount)
        // Every write is still recorded so an in-flight sync can detect them.
        assertEquals(3, tracker.snapshot())
    }

    @Test
    fun `each write marks sync pending and records a change`() = runTest {
        val scheduler = FakeBackupScheduler()
        val prefs = enabledPrefs()
        val tracker = BackupChangeTracker()
        val coordinator = coordinator(scheduler, prefs, tracker, backgroundScope)

        coordinator.onDataChanged()
        runCurrent()

        assertTrue(prefs.syncPending)
        assertEquals(1, tracker.snapshot())
    }

    @Test
    fun `does nothing when backup is disabled`() = runTest {
        val scheduler = FakeBackupScheduler()
        val prefs =
            FakeBackupPreferences().apply { backupEnabled = false; syncOnSaveEnabled = true }
        val tracker = BackupChangeTracker()
        val coordinator = coordinator(scheduler, prefs, tracker, backgroundScope)

        coordinator.onDataChanged()
        advanceTimeBy((debounce + 100).milliseconds)
        runCurrent()

        assertEquals(0, scheduler.triggerOnChangeSyncCount)
        assertEquals(0, tracker.snapshot())
        assertFalse(prefs.syncPending)
    }

    @Test
    fun `does nothing when sync-on-save is off`() = runTest {
        val scheduler = FakeBackupScheduler()
        val prefs =
            FakeBackupPreferences().apply { backupEnabled = true; syncOnSaveEnabled = false }
        val tracker = BackupChangeTracker()
        val coordinator = coordinator(scheduler, prefs, tracker, backgroundScope)

        coordinator.onDataChanged()
        advanceTimeBy((debounce + 100).milliseconds)
        runCurrent()

        assertEquals(0, scheduler.triggerOnChangeSyncCount)
        assertEquals(0, tracker.snapshot())
    }
}
