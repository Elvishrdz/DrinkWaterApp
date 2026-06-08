package com.eahm.drinkwaterapp.backup.data.usecase

import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser

class ConnectBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val syncUseCase: SyncBackupUseCase,
) {
    suspend operator fun invoke(
        providerType: CloudProviderType,
    ): BackupResult<CloudUser> {
        // Sign out from current provider if different
        val currentId = prefs.getBackupProvider()
        if (currentId != null && currentId != providerType.id) {
            val currentType = CloudProviderType.fromId(currentId)
            if (currentType != null) registry.get(currentType).signOut()
        }

        val provider = registry.get(providerType)

        val silentResult = provider.silentSignIn()
        val user = when (silentResult) {
            is BackupResult.Success -> silentResult.value
            is BackupResult.Failure -> return silentResult
        }

        // Persist connection
        prefs.setBackupEnabled(true)
        prefs.setBackupProvider(providerType.id)
        prefs.setBackupUserDisplayName(user.displayName)

        // First sync
        syncUseCase(provider)

        return BackupResult.Success(user)
    }
}
