package com.eahm.drinkwaterapp.backup.domain.model

sealed interface SyncResult {
    data object Uploaded : SyncResult
    data object SkippedNoChanges : SyncResult
    data object AlreadyInProgress : SyncResult
}
