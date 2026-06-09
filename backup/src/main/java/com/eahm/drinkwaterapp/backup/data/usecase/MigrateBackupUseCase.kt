package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupSyncLock
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult

class MigrateBackupUseCase(
    private val lock: BackupSyncLock,
    private val prefs: BackupPreferences,
) {
    suspend operator fun invoke(
        from: CloudBackupProvider,
        to: CloudBackupProvider,
    ): BackupResult<Unit> {
        if (from === to) return BackupResult.Failure(BackupError.SameProviderMigration)

        if (!lock.mutex.tryLock()) return BackupResult.Failure(BackupError.Unknown(null))
        return try {
            val backupFile = when (val dl = from.download()) {
                is BackupResult.Success -> dl.value
                is BackupResult.Failure -> return dl
            }

            val uploadResult = to.upload(backupFile.rawData.toString())
            if (uploadResult is BackupResult.Failure) return uploadResult

            // Persist new provider
            prefs.setBackupProvider(to.providerType.id)

            BackupResult.Success(Unit)
        } finally {
            lock.mutex.unlock()
        }
    }
}
