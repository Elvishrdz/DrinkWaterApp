package com.eahm.drinkwaterapp.backup

class BackupProviderRegistry(
    private val googleDriveProvider: CloudBackupProvider,
    private val dropboxProvider: CloudBackupProvider,
) {
    fun get(type: CloudProviderType): CloudBackupProvider = when (type) {
        CloudProviderType.GOOGLE_DRIVE -> googleDriveProvider
        CloudProviderType.DROPBOX -> dropboxProvider
    }
}
