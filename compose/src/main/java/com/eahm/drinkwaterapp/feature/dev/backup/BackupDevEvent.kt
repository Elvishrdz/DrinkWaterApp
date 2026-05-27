package com.eahm.drinkwaterapp.feature.dev.backup

sealed interface BackupDevEvent {

    data class ShowMessage(val text: String) : BackupDevEvent
}
