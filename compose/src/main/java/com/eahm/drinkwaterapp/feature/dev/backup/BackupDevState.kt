package com.eahm.drinkwaterapp.feature.dev.backup

import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.core.backup.BackupNotificationMode

data class BackupDevState(
    val syncState: BackupSyncState = BackupSyncState.Idle,
    val log: List<String> = emptyList(),
    /** Human-readable unified sync status (running / last outcome + trigger + disposition). */
    val syncStatusLabel: String = "Idle",
    /** WorkManager state summary per unique backup work name. */
    val workInfoSummary: String = "No work enqueued",
    /** Dev toggle: when the dedicated backup-status notification is shown. */
    val notificationMode: BackupNotificationMode = BackupNotificationMode.OFF,
    /** Total number of records merged across all sync sessions this run. */
    val mergedRecordCount: Int = 0,
    /** Total tombstones applied since app start. */
    val tombstonesApplied: Int = 0,
    /** Total auto-resolved conflicts since app start. */
    val autoResolvedConflicts: Int = 0,
    /** Pretty-printed remote backup JSON to inspect; non-null shows the viewer sheet. */
    val backupPreview: String? = null,
)
