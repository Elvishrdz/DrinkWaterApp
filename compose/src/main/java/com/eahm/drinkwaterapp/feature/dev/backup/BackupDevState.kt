package com.eahm.drinkwaterapp.feature.dev.backup

import com.eahm.drinkwaterapp.backup.domain.BackupSyncState

data class BackupDevState(
    val syncState: BackupSyncState = BackupSyncState.Idle,
    val log: List<String> = emptyList(),
    /** Total number of records merged across all sync sessions this run. */
    val mergedRecordCount: Int = 0,
    /** Total tombstones applied since app start. */
    val tombstonesApplied: Int = 0,
    /** Total auto-resolved conflicts since app start. */
    val autoResolvedConflicts: Int = 0,
    /** Pretty-printed remote backup JSON to inspect; non-null shows the viewer sheet. */
    val backupPreview: String? = null,
)
