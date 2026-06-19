package com.eahm.drinkwaterapp.feature.onboarding.backup

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.InitialSyncResolver
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.provider.GoogleDriveBackupProvider
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.feature.settings.backup.BackupUiState
import com.eahm.drinkwaterapp.feature.settings.backup.toUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the Cloud-Backup step during onboarding. Mirrors the backup connect/sync/resolve flow from
 * [com.eahm.drinkwaterapp.feature.settings.SettingsViewModel] so new users get the exact same
 * experience (Google sign-in → connect → first sync with conflict/initial-sync resolution), kept
 * isolated from Settings so neither flow can break the other.
 */
internal class OnboardingBackupViewModel(
    private val backupRepository: BackupRepository,
    private val backupPreferences: BackupPreferences,
    private val googleDriveProvider: GoogleDriveBackupProvider,
    private val syncConflictResolver: SyncConflictResolver,
    private val backupRecoveryResolver: BackupRecoveryResolver,
    private val initialSyncResolver: InitialSyncResolver,
) : ViewModel() {

    private val _backupUiState = MutableStateFlow(BackupUiState())

    val state = _backupUiState
        .combine(backupRepository.syncStatus) { state, status ->
            state.copy(
                isSyncing = status.running,
                error = (status.lastOutcome as? BackupSyncStatus.Outcome.Failure)?.error?.toUI(),
            )
        }
        .combine(syncConflictResolver.pendingConflict) { state, conflict ->
            state.copy(pendingConflict = conflict)
        }
        .combine(backupRecoveryResolver.pendingCorruption) { state, corruption ->
            state.copy(pendingCorruption = corruption)
        }
        .combine(initialSyncResolver.pending) { state, initialSync ->
            state.copy(pendingInitialSync = initialSync)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = _backupUiState.value,
        )

    init {
        viewModelScope.launch { refreshBackupState() }
    }

    private suspend fun refreshBackupState() {
        val providerType = backupPreferences.getBackupProvider()
            ?.let { CloudProviderType.fromId(it) }
        _backupUiState.update {
            it.copy(
                isConnected = providerType != null,
                providerType = providerType,
                lastSyncAt = backupPreferences.getBackupLastSyncAt(),
            )
        }
    }

    suspend fun getGoogleAuthorizationIntentSender(): IntentSender? =
        googleDriveProvider.getAuthorizationIntentSender()

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            val result = googleDriveProvider.handleSignInResult(
                resultCode = Activity.RESULT_OK,
                data = data,
            )
            if (result is BackupResult.Success) {
                backupRepository.connect(CloudProviderType.GOOGLE_DRIVE)
                refreshBackupState()
                backupRepository.sync(allowInitialDecision = true)
                refreshBackupState()
            }
        }
    }

    fun onResolveConflict(useRemote: Boolean) {
        syncConflictResolver.resolve(useRemote)
    }

    fun onResolveCorruption(overwrite: Boolean) {
        backupRecoveryResolver.resolve(overwrite)
        if (overwrite) {
            viewModelScope.launch { refreshBackupState() }
        }
    }

    fun onResolveInitialSync(decision: InitialSyncDecision) {
        initialSyncResolver.resolve(decision)
        viewModelScope.launch { refreshBackupState() }
    }

    fun onDismissInitialSync() {
        initialSyncResolver.cancelPending()
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
