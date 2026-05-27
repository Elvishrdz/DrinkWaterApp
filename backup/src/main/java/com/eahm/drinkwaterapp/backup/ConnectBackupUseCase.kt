package com.eahm.drinkwaterapp.backup

import android.content.Context

class ConnectBackupUseCase(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val syncUseCase: SyncBackupUseCase,
) {
    suspend operator fun invoke(
        context: Context,
        providerType: CloudProviderType,
    ): BackupResult<CloudUser> {
        // Sign out from current provider if different
        val currentId = prefs.getBackupProvider()
        if (currentId != null && currentId != providerType.id) {
            val currentType = CloudProviderType.fromId(currentId)
            if (currentType != null) registry.get(currentType).signOut(context)
        }

        val provider = registry.get(providerType)

        val silentResult = provider.silentSignIn(context)
        val user = when (silentResult) {
            is BackupResult.Success -> silentResult.value
            is BackupResult.Failure -> return silentResult
        }

        // Persist connection
        prefs.setBackupEnabled(true)
        prefs.setBackupProvider(providerType.id)
        prefs.setBackupUserDisplayName(user.displayName)

        // First sync
        syncUseCase(context, provider)

        return BackupResult.Success(user)
    }
}
