package com.eahm.drinkwaterapp.backup

class BackupProviderRegistry(
    private val googleDriveProvider: GoogleDriveBackupProvider,
    private val dropboxProvider: DropboxBackupProvider,
) {
    fun get(type: CloudProviderType): CloudBackupProvider = when (type) {
        CloudProviderType.GOOGLE_DRIVE -> googleDriveProvider
        CloudProviderType.DROPBOX -> dropboxProvider
    }
}
