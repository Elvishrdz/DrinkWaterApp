package com.eahm.drinkwaterapp.backup.domain.model

data class RemoteBackupStatus(
    val metadata: BackupMetadata,
    val isCompatible: Boolean,
)
