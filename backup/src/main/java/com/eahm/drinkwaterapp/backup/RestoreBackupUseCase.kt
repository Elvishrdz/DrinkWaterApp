package com.eahm.drinkwaterapp.backup

import android.content.Context

class RestoreBackupUseCase(
    private val dataSource: BackupDataSource,
) {
    suspend operator fun invoke(
        context: Context,
        provider: CloudBackupProvider,
    ): BackupResult<Unit> {
        val downloadResult = provider.download(context)
        val backupFile = when (downloadResult) {
            is BackupResult.Success -> downloadResult.value
            is BackupResult.Failure -> return downloadResult
        }

        val remoteSchema = backupFile.metadata.schemaVersion
        if (remoteSchema > BackupMigrationManager.currentSchema) {
            return BackupResult.Failure(BackupError.IncompatibleVersion(remoteSchema))
        }

        val migratedData =
            when (val m = BackupMigrationManager.migrate(backupFile.rawData, remoteSchema)) {
                is BackupResult.Success -> m.value
                is BackupResult.Failure -> return m
            }

        return dataSource.deserializeAndImport(migratedData)
    }
}
