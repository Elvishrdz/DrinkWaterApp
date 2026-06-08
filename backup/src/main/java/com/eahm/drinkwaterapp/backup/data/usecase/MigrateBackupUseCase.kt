package com.eahm.drinkwaterapp.backup.data.usecase

import android.content.Context
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.coroutines.sync.Mutex

class MigrateBackupUseCase(
    private val mutex: Mutex,
    private val prefs: BackupPreferences,
) {
    suspend operator fun invoke(
        context: Context?,
        from: CloudBackupProvider,
        to: CloudBackupProvider,
    ): BackupResult<Unit> {
        if (from === to) return BackupResult.Failure(BackupError.SameProviderMigration)

        if (!mutex.tryLock()) return BackupResult.Failure(BackupError.Unknown(null))
        return try {
            val backupFile = when (val dl = from.download(context)) {
                is BackupResult.Success -> dl.value
                is BackupResult.Failure -> return dl
            }

            val uploadResult = to.upload(context, backupFile.rawData.toString())
            if (uploadResult is BackupResult.Failure) return uploadResult

            // Persist new provider
            prefs.setBackupProvider(to.providerType.id)

            BackupResult.Success(Unit)
        } finally {
            mutex.unlock()
        }
    }
}
