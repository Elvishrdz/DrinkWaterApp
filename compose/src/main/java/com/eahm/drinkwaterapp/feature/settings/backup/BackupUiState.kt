package com.eahm.drinkwaterapp.feature.settings.backup

import com.eahm.drinkwaterapp.backup.CloudProviderType
import com.eahm.drinkwaterapp.backup.SeenDevice
import com.eahm.drinkwaterapp.backup.SyncConflict

data class BackupUiState(
    val isConnected: Boolean = false,
    val providerType: CloudProviderType? = null,
    val lastSyncAt: Long? = null,
    val isSyncing: Boolean = false,
    /** True when a sync is pending (data changed but not yet uploaded). */
    val syncPending: Boolean = false,
    /** Whether "sync on save" is enabled (upload after each drink/goal write). */
    val syncOnSaveEnabled: Boolean = true,
    /** All devices seen sharing this account's backup. */
    val seenDevices: List<SeenDevice> = emptyList(),
    /** Non-null when the sync engine is waiting for user conflict resolution. */
    val pendingConflict: SyncConflict? = null,
)
