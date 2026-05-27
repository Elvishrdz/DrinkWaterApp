package com.eahm.drinkwaterapp.feature.dev.backup

import com.eahm.drinkwaterapp.backup.BackupSyncState

data class BackupDevState(
    val syncState: BackupSyncState = BackupSyncState.Idle,
    val log: List<String> = emptyList(),
)
