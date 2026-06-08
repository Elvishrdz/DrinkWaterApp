package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CorruptedBackup
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict

@Immutable
data class BackupUiState(
    val isConnected: Boolean = false,
    val providerType: CloudProviderType? = null,
    val lastSyncAt: Long? = null,
    val isSyncing: Boolean = false,
    // True when a sync is pending (data changed but not yet uploaded).
    val syncPending: Boolean = false,
    // Save whether upload after each drink/goal write is performed.
    val syncOnSaveEnabled: Boolean = true,
    // All devices seen sharing this account's backup.
    val seenDevices: List<SeenDevice> = emptyList(),
    // Non-null when the sync engine is waiting for user conflict resolution.
    val pendingConflict: SyncConflict? = null,
    // Non-null when the remote backup is corrupted and awaiting a user recovery decision.
    val pendingCorruption: CorruptedBackup? = null,
    val error: BackupUIError? = null,
)

enum class BackupUIError {
    NOT_SIGNED_IN,
    UNKNOWN,
}

internal fun BackupError.toUI(): BackupUIError {
    return when {
        this is BackupError.NotSignedIn -> BackupUIError.NOT_SIGNED_IN
        else -> BackupUIError.UNKNOWN
    }
}
