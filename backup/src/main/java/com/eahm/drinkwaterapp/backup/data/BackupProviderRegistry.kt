package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType

class BackupProviderRegistry(
    private val googleDriveProvider: CloudBackupProvider,
    private val dropboxProvider: CloudBackupProvider,
) {
    fun get(type: CloudProviderType): CloudBackupProvider = when (type) {
        CloudProviderType.GOOGLE_DRIVE -> googleDriveProvider
        CloudProviderType.DROPBOX -> dropboxProvider
    }
}
