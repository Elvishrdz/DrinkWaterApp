package com.eahm.drinkwaterapp.backup

import android.content.Context

class CheckBackupStatusUseCase {
    suspend operator fun invoke(
        context: Context,
        provider: CloudBackupProvider,
    ): BackupResult<RemoteBackupStatus?> {
        return when (val result = provider.getRemoteMetadata(context)) {
            is BackupResult.Success -> {
                val metadata = result.value ?: return BackupResult.Success(null)
                val isCompatible = metadata.schemaVersion <= BackupMigrationManager.currentSchema
                BackupResult.Success(
                    RemoteBackupStatus(
                        metadata = metadata,
                        isCompatible = isCompatible
                    )
                )
            }

            is BackupResult.Failure -> result
        }
    }
}
