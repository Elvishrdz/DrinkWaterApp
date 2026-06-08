package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton bridge between [com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase] (which suspends waiting for user input)
 * and the UI (which shows conflict resolution sheets).
 *
 * Usage by use case: call [awaitResolution] for each conflict — suspends until [resolve] is called.
 * Usage by ViewModel: collect [pendingConflict] to show/hide the sheet; call [resolve] on user choice.
 */
class SyncConflictResolver {

    private val _pendingConflict = MutableStateFlow<SyncConflict?>(null)
    val pendingConflict: StateFlow<SyncConflict?> = _pendingConflict.asStateFlow()

    @Volatile
    private var deferred: CompletableDeferred<Boolean>? = null

    /**
     * Emits [conflict] to the UI and suspends until [resolve] is called.
     * Returns true if the user chose to use the remote version, false for local.
     */
    suspend fun awaitResolution(conflict: SyncConflict): Boolean {
        val d = CompletableDeferred<Boolean>()
        deferred = d
        _pendingConflict.value = conflict
        return try {
            d.await()
        } finally {
            _pendingConflict.value = null
        }
    }

    /**
     * Called by the UI when the user makes a choice.
     * [useRemote] = true → use the remote version; false → keep local.
     */
    fun resolve(useRemote: Boolean) {
        deferred?.complete(useRemote)
    }

    /**
     * Cancel any pending resolution (e.g. if the sync is cancelled or the app goes background).
     * Defaults to keeping local (useRemote = false).
     */
    fun cancelPending() {
        deferred?.complete(false)
        _pendingConflict.value = null
    }
}
