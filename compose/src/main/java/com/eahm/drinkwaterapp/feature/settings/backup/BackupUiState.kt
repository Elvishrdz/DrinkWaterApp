package com.eahm.drinkwaterapp.feature.settings.backup

import com.eahm.drinkwaterapp.backup.CloudProviderType

data class BackupUiState(
    val isConnected: Boolean = false,
    val providerType: CloudProviderType? = null,
    val lastSyncAt: Long? = null,
    val isSyncing: Boolean = false,
)
