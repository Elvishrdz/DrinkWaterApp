package com.eahm.drinkwaterapp.backup.domain

import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict

sealed interface BackupSyncState {
    data object Idle : BackupSyncState
    data object InProgress : BackupSyncState
    data object UpToDate : BackupSyncState
    data class Success(val syncedAt: Long) : BackupSyncState
    data class Error(val error: BackupError) : BackupSyncState
    data class ConflictDetected(val conflicts: List<SyncConflict>) : BackupSyncState
}
