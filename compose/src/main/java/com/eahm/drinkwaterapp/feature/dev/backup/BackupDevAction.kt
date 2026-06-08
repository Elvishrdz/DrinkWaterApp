package com.eahm.drinkwaterapp.feature.dev.backup

sealed interface BackupDevAction {
    // Google Drive
    data object ConnectGoogle : BackupDevAction
    data object SyncGoogle : BackupDevAction
    data object RestoreGoogle : BackupDevAction
    data object CheckStatusGoogle : BackupDevAction
    data object DisconnectGoogle : BackupDevAction
    data object DeleteFileGoogle : BackupDevAction

    // Dropbox
    data object ConnectDropbox : BackupDevAction
    data object SyncDropbox : BackupDevAction
    data object RestoreDropbox : BackupDevAction
    data object CheckStatusDropbox : BackupDevAction
    data object DisconnectDropbox : BackupDevAction
    data object DeleteFileDropbox : BackupDevAction

    // Migration
    data object MigrateGoogleToDropbox : BackupDevAction
    data object MigrateDropboxToGoogle : BackupDevAction

    // Sync simulation (dev-only)
    data object SimulateGoalConflict : BackupDevAction
    data object SimulatePrefsConflict : BackupDevAction
    data object SimulateTombstoneWarning : BackupDevAction

    // Log
    data object ClearLog : BackupDevAction
}
