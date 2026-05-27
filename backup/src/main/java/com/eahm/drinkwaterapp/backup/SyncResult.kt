package com.eahm.drinkwaterapp.backup

sealed interface SyncResult {
    data object Uploaded : SyncResult
    data object SkippedNoChanges : SyncResult
    data object AlreadyInProgress : SyncResult
}
