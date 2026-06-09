package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CorruptedBackup
import com.eahm.drinkwaterapp.backup.domain.model.ExistingRemoteBackup
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict

@Immutable
data class BackupUiState(
    val isConnected: Boolean = false,
    // Hour of day (0–23) the daily auto-backup runs.
    val autoBackupHour: Int = 2,
    // Contextual status/error message for the backup section.
    val statusMessage: BackupStatusMessage = BackupStatusMessage.NONE,
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
    // Non-null on first connection when a remote backup already exists and the user must
    // choose whether to keep the remote data or replace it with this device's data.
    val pendingInitialSync: ExistingRemoteBackup? = null,
    val error: BackupUIError? = null,
)

enum class BackupUIError {
    NOT_SIGNED_IN,
    UNKNOWN,
}

/**
 * Contextual backup status shown in Settings. Derived from the unified sync status (trigger +
 * disposition) plus pending/connectivity state. Maps to a user-facing string in the UI layer.
 */
enum class BackupStatusMessage {
    NONE,

    /** A sync is running right now. */
    SYNCING,

    /** Pending changes can't upload until the connection returns — retry is guaranteed. */
    WAITING_FOR_CONNECTION,

    /** A background sync failed but will be retried automatically soon. */
    WILL_RETRY,

    /** An on-save backup failed; it will be retried automatically on the next change/sync. */
    ON_SAVE_RETRY,

    /** Sync failed with no guaranteed retry — prompt the user to sync manually. */
    FAILED_USE_MANUAL,
}

internal fun BackupError.toUI(): BackupUIError {
    return when {
        this is BackupError.NotSignedIn -> BackupUIError.NOT_SIGNED_IN
        else -> BackupUIError.UNKNOWN
    }
}
