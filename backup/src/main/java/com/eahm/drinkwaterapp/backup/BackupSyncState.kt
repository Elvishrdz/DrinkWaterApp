package com.eahm.drinkwaterapp.backup

sealed interface BackupSyncState {
    data object Idle : BackupSyncState
    data object InProgress : BackupSyncState
    data object UpToDate : BackupSyncState
    data class Success(val syncedAt: Long) : BackupSyncState
    data class Error(val error: BackupError) : BackupSyncState
}
