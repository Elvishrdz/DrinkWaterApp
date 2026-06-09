package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncDisposition
import com.eahm.drinkwaterapp.backup.domain.model.SyncTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-wide single source of truth for backup sync progress.
 *
 * Every sync path — interactive "Sync now", connect, and the background [CloudBackupWorker]
 * (periodic / on-save / foreground / reconnect) — reports here, so the Settings UI shows
 * "Syncing…" and the right error copy regardless of who started the sync.
 *
 * Exposes [status] (rich, with trigger + disposition) and [legacyState] (the older
 * [BackupSyncState] shape) so existing observers keep working.
 */
class BackupSyncStatusHolder(private val prefs: BackupPreferences) {

    private val _status = MutableStateFlow(BackupSyncStatus())
    val status: StateFlow<BackupSyncStatus> = _status.asStateFlow()

    private val _legacyState = MutableStateFlow<BackupSyncState>(BackupSyncState.Idle)
    val legacyState: StateFlow<BackupSyncState> = _legacyState.asStateFlow()

    /** True while any sync is running. Used as the cross-path guard. */
    val isRunning: Boolean get() = _status.value.running

    fun onStarted(trigger: SyncTrigger) {
        _status.update { it.copy(running = true, activeTrigger = trigger) }
        _legacyState.value = BackupSyncState.InProgress
    }

    suspend fun onSuccess(trigger: SyncTrigger, syncedAt: Long) {
        _status.update {
            it.copy(
                running = false,
                activeTrigger = null,
                lastOutcome = BackupSyncStatus.Outcome.Success(syncedAt, trigger),
            )
        }
        _legacyState.value = BackupSyncState.Success(syncedAt)
        // Persist the completion time for EVERY successful sync (upload or no-op "no changes"),
        // from any source, so the Settings "Last backup" text stays current. SyncBackupUseCase
        // only writes this on an actual upload, which is why background no-op syncs left it stale.
        prefs.setBackupLastSyncAt(syncedAt)
        prefs.setBackupLastError(null)
        prefs.setBackupLastFailure(null)
    }

    suspend fun onFailure(
        trigger: SyncTrigger,
        error: BackupError,
        disposition: SyncDisposition,
    ) {
        _status.update {
            it.copy(
                running = false,
                activeTrigger = null,
                lastOutcome = BackupSyncStatus.Outcome.Failure(error, trigger, disposition),
            )
        }
        _legacyState.value = BackupSyncState.Error(error)
        prefs.setBackupLastError(error.javaClass.simpleName)
        prefs.setBackupLastFailure("${trigger.name}|${disposition.name}")
    }

    /**
     * A sync was skipped because another was already running (lost the lock race). Clear the
     * running flag without overwriting the last real outcome.
     */
    fun onSkipped() {
        _status.update { it.copy(running = false, activeTrigger = null) }
        if (_legacyState.value is BackupSyncState.InProgress) {
            _legacyState.value = BackupSyncState.Idle
        }
    }
}
