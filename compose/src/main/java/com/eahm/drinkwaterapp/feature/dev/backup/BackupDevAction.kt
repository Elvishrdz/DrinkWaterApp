package com.eahm.drinkwaterapp.feature.dev.backup

sealed interface BackupDevAction {
    // Google Drive
    data object ConnectGoogle : BackupDevAction
    data object SyncGoogle : BackupDevAction
    data object RestoreGoogle : BackupDevAction
    data object CheckStatusGoogle : BackupDevAction
    data object DisconnectGoogle : BackupDevAction
    data object DeleteFileGoogle : BackupDevAction
    data object VisualizeGoogle : BackupDevAction

    // Dropbox
    data object ConnectDropbox : BackupDevAction
    data object SyncDropbox : BackupDevAction
    data object RestoreDropbox : BackupDevAction
    data object CheckStatusDropbox : BackupDevAction
    data object DisconnectDropbox : BackupDevAction
    data object DeleteFileDropbox : BackupDevAction
    data object VisualizeDropbox : BackupDevAction

    // Backup viewer
    data object DismissBackupPreview : BackupDevAction

    // Migration
    data object MigrateGoogleToDropbox : BackupDevAction
    data object MigrateDropboxToGoogle : BackupDevAction

    // Sync simulation (dev-only)
    data object SimulateGoalConflict : BackupDevAction
    data object SimulatePrefsConflict : BackupDevAction
    data object SimulateTombstoneWarning : BackupDevAction

    // Background work (WorkManager) controls
    data object RunBackgroundSyncNow : BackupDevAction
    data object TriggerOnChangeSync : BackupDevAction
    data object TriggerForegroundSync : BackupDevAction
    data object ScheduleDailySync : BackupDevAction
    data object CancelAllWork : BackupDevAction
    data class SetBackupNotification(val enabled: Boolean) : BackupDevAction

    // Log
    data object ClearLog : BackupDevAction
}
