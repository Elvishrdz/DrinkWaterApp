package com.eahm.drinkwaterapp.backup

import android.content.Context

class DisconnectBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val scheduler: BackupScheduler,
) {
    suspend operator fun invoke(context: Context): BackupResult<Unit> {
        val providerId = prefs.getBackupProvider()
        val type = providerId?.let { CloudProviderType.fromId(it) }

        if (type != null) {
            registry.get(type).signOut(context)
        }

        prefs.clearBackupKeys()
        scheduler.cancelAll()

        return BackupResult.Success(Unit)
    }
}
