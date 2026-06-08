package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType

class DisconnectBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val scheduler: BackupScheduler,
) {
    suspend operator fun invoke(): BackupResult<Unit> {
        val providerId = prefs.getBackupProvider()
        val type = providerId?.let { CloudProviderType.fromId(it) }

        if (type != null) {
            registry.get(type).signOut()
        }

        prefs.clearBackupKeys()
        scheduler.cancelAll()

        return BackupResult.Success(Unit)
    }
}
