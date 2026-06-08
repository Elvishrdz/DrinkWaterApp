package com.eahm.drinkwaterapp.backup.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CloudBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val syncUseCase: SyncBackupUseCase by inject()

    override suspend fun doWork(): Result {
        return when (val result =
            syncUseCase(resolveProvider() ?: return Result.failure())) {
            is BackupResult.Success -> Result.success()
            is BackupResult.Failure -> {
                val error = result.error
                if (error is BackupError.NetworkUnavailable || error is BackupError.Unknown) {
                    if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
                } else {
                    Result.failure()
                }
            }
        }
    }

    private suspend fun resolveProvider(): CloudBackupProvider? {
        val prefs: BackupPreferences by inject()
        val registry: BackupProviderRegistry by inject()
        val id = prefs.getBackupProvider() ?: return null
        val type = CloudProviderType.fromId(id) ?: return null
        return registry.get(type)
    }

    companion object {
        const val WORK_NAME_PERIODIC = "BACKUP_SYNC_WORK_PERIODIC"
        const val WORK_NAME_MANUAL = "BACKUP_SYNC_WORK_MANUAL"
        const val WORK_NAME_ON_CHANGE = "cloud_backup_sync_on_change"
        const val WORK_NAME_FOREGROUND = "cloud_backup_sync_foreground"
        private const val MAX_RETRIES = 3
    }
}
