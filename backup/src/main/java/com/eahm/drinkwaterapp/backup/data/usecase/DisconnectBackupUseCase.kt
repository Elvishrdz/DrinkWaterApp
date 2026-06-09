package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType

class DisconnectBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val scheduler: BackupScheduler,
    private val dataSource: BackupDataSource,
) {
    suspend operator fun invoke(): BackupResult<Unit> {
        val providerId = prefs.getBackupProvider()
        val type = providerId?.let { CloudProviderType.fromId(it) }

        if (type != null) {
            registry.get(type).signOut()
        }

        prefs.clearBackupKeys()
        scheduler.cancelAll()

        // Tombstones are a sync artifact; with backup off they have no purpose. Physically
        // remove all of them (threshold = MAX_VALUE matches every deleted_at). New deletes
        // while disconnected are hard-deleted at the source (see history ViewModels).
        dataSource.purgeExpiredTombstones(Long.MAX_VALUE)

        return BackupResult.Success(Unit)
    }
}
