package com.eahm.drinkwaterapp.backup.data.usecase

import android.content.Context
import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.RemoteBackupStatus

class CheckBackupStatusUseCase {
    suspend operator fun invoke(
        context: Context?,
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
