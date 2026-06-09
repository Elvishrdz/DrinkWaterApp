package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupSyncStatusHolder
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncDisposition
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackupSyncStatusHolderTest {

    @Test
    fun `onStarted marks running and InProgress`() {
        val holder = BackupSyncStatusHolder(FakeBackupPreferences())
        holder.onStarted(SyncTrigger.PERIODIC)

        assertTrue(holder.isRunning)
        assertEquals(SyncTrigger.PERIODIC, holder.status.value.activeTrigger)
        assertTrue(holder.legacyState.value is BackupSyncState.InProgress)
    }

    @Test
    fun `onSuccess clears running, records outcome and clears persisted failure`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupLastError = "Boom"
            backupLastFailure = "PERIODIC|FAILED"
        }
        val holder = BackupSyncStatusHolder(prefs)
        holder.onStarted(SyncTrigger.MANUAL)
        holder.onSuccess(SyncTrigger.MANUAL, syncedAt = 123L)

        assertFalse(holder.isRunning)
        val outcome = holder.status.value.lastOutcome
        assertTrue(outcome is BackupSyncStatus.Outcome.Success)
        assertEquals(123L, outcome.syncedAt)
        // Every successful sync (incl. no-op) must persist the completion time so the
        // "Last backup" text stays current across all sources.
        assertEquals(123L, prefs.backupLastSyncAt)
        assertNull(prefs.backupLastError)
        assertNull(prefs.backupLastFailure)
    }

    @Test
    fun `onFailure records disposition and persists failure descriptor`() = runTest {
        val prefs = FakeBackupPreferences()
        val holder = BackupSyncStatusHolder(prefs)
        holder.onStarted(SyncTrigger.ON_SAVE)
        holder.onFailure(
            SyncTrigger.ON_SAVE,
            BackupError.NetworkUnavailable,
            SyncDisposition.WAITING_FOR_CONNECTION,
        )

        assertFalse(holder.isRunning)
        val outcome = holder.status.value.lastOutcome
        assertTrue(outcome is BackupSyncStatus.Outcome.Failure)
        assertEquals(SyncDisposition.WAITING_FOR_CONNECTION, outcome.disposition)
        assertEquals("ON_SAVE|WAITING_FOR_CONNECTION", prefs.backupLastFailure)
        assertTrue(holder.legacyState.value is BackupSyncState.Error)
    }

    @Test
    fun `onSkipped clears running without overwriting last outcome`() = runTest {
        val holder = BackupSyncStatusHolder(FakeBackupPreferences())
        holder.onStarted(SyncTrigger.MANUAL)
        holder.onSuccess(SyncTrigger.MANUAL, syncedAt = 1L)

        holder.onStarted(SyncTrigger.PERIODIC)
        holder.onSkipped()

        assertFalse(holder.isRunning)
        // The earlier success must survive a skipped run.
        assertTrue(holder.status.value.lastOutcome is BackupSyncStatus.Outcome.Success)
    }
}
