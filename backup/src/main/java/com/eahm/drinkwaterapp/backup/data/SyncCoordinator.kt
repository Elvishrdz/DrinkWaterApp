package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coordinates reactive sync triggers. Call [onDataChanged] after any local write.
 * Debounces rapid successive writes and enqueues a WorkManager sync job after the window elapses.
 *
 * Also marks KEY_SYNC_PENDING=true immediately on every write so the UI can show a
 * "pending changes" indicator even before the debounce fires.
 */
class SyncCoordinator(
    private val scheduler: BackupScheduler,
    private val prefs: BackupPreferences,
    private val debounceMs: Long = 30_000L,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {

    private var debounceJob: Job? = null

    /**
     * Call this after every successful local DB write. Safe to call from any thread.
     * If backup is disabled, this is a no-op.
     */
    fun onDataChanged() {
        scope.launch {
            if (!prefs.getBackupEnabled()) return@launch
            if (!prefs.getSyncOnSaveEnabled()) return@launch

            // Mark pending immediately so the UI can react
            prefs.setSyncPending(true)

            // Cancel any pending debounce and restart
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(debounceMs)
                scheduler.triggerOnChangeSync()
            }
        }
    }
}
