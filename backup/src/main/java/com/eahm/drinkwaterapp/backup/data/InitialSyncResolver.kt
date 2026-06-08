package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.model.ExistingRemoteBackup
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton bridge between the connect flow (which suspends on first connection when a
 * remote backup already exists) and the UI (which shows a "use remote or upload local"
 * choice dialog).
 *
 * Mirrors [BackupRecoveryResolver]: the connect use case calls [awaitDecision] and
 * suspends until the UI calls [resolve]; the UI observes [pending] to show/hide the
 * dialog. A null result means the user dismissed the dialog without choosing.
 */
class InitialSyncResolver {

    private val _pending = MutableStateFlow<ExistingRemoteBackup?>(null)
    val pending: StateFlow<ExistingRemoteBackup?> = _pending.asStateFlow()

    @Volatile
    private var deferred: CompletableDeferred<InitialSyncDecision?>? = null

    /**
     * Emits [remote] to the UI and suspends until [resolve] (or [cancelPending]) is called.
     * Returns the chosen [InitialSyncDecision], or null if the user dismissed the dialog.
     */
    suspend fun awaitDecision(remote: ExistingRemoteBackup): InitialSyncDecision? {
        val d = CompletableDeferred<InitialSyncDecision?>()
        deferred = d
        _pending.value = remote
        return try {
            d.await()
        } finally {
            _pending.value = null
        }
    }

    /** Called by the UI when the user picks an option. */
    fun resolve(decision: InitialSyncDecision) {
        deferred?.complete(decision)
    }

    /** Cancel any pending decision (dialog dismissed / flow cancelled). */
    fun cancelPending() {
        deferred?.complete(null)
        _pending.value = null
    }
}
