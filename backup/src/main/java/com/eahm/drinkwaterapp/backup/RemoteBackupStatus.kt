package com.eahm.drinkwaterapp.backup

data class RemoteBackupStatus(
    val metadata: BackupMetadata,
    val isCompatible: Boolean,
)
