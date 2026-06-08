package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.model.CorruptedBackup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton bridge between [com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase] (which suspends when it finds a remote
 * backup it cannot recognize) and the UI (which shows a destructive, irreversible
 * "overwrite corrupted backup?" dialog).
 *
 * Mirrors [SyncConflictResolver]: the use case calls [awaitDecision] and suspends until
 * the UI calls [resolve]; the UI observes [pendingCorruption] to show/hide the dialog.
 */
class BackupRecoveryResolver {

    private val _pendingCorruption = MutableStateFlow<CorruptedBackup?>(null)
    val pendingCorruption: StateFlow<CorruptedBackup?> = _pendingCorruption.asStateFlow()

    @Volatile
    private var deferred: CompletableDeferred<Boolean>? = null

    /**
     * Emits [corruption] to the UI and suspends until [resolve] is called.
     * Returns true if the user chose to overwrite the remote file with local data,
     * false to leave the remote backup untouched.
     */
    suspend fun awaitDecision(corruption: CorruptedBackup): Boolean {
        val d = CompletableDeferred<Boolean>()
        deferred = d
        _pendingCorruption.value = corruption
        return try {
            d.await()
        } finally {
            _pendingCorruption.value = null
        }
    }

    /**
     * Called by the UI when the user makes a choice.
     * [overwrite] = true → replace the remote file with local data; false → do nothing.
     */
    fun resolve(overwrite: Boolean) {
        deferred?.complete(overwrite)
    }

    /** Cancel any pending decision (e.g. sync cancelled). Defaults to leaving remote untouched. */
    fun cancelPending() {
        deferred?.complete(false)
        _pendingCorruption.value = null
    }
}
