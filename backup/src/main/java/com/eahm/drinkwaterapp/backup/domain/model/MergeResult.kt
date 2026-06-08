package com.eahm.drinkwaterapp.backup.domain.model

data class MergeResult(
    val mergedData: BackupData,
    val appliedRemoteRecords: Int,
    val autoResolvedConflicts: Int,
    val tombstonesApplied: Int,
    val conflicts: List<SyncConflict>,
)
