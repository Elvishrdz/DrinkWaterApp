package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.fake.FakeBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.FakeBackupScheduler
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class DisconnectBackupUseCaseTest {

    private fun buildRegistry(vararg providers: FakeCloudBackupProvider): BackupProviderRegistry {
        val google = providers.firstOrNull { it.providerType == CloudProviderType.GOOGLE_DRIVE }
            ?: FakeCloudBackupProvider(CloudProviderType.GOOGLE_DRIVE)
        val dropbox = providers.firstOrNull { it.providerType == CloudProviderType.DROPBOX }
            ?: FakeCloudBackupProvider(CloudProviderType.DROPBOX)
        return BackupProviderRegistry(google, dropbox)
    }

    @Test
    fun `disconnect signs out of current provider`() = runTest {
        val provider = FakeCloudBackupProvider(CloudProviderType.GOOGLE_DRIVE)
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
        }
        val useCase = DisconnectBackupUseCase(buildRegistry(provider), prefs, FakeBackupScheduler(), FakeBackupDataSource())

        useCase()

        assertTrue(provider.signOutCalled)
    }

    @Test
    fun `disconnect clears backup keys`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
            backupEnabled = true
        }
        val useCase = DisconnectBackupUseCase(buildRegistry(), prefs, FakeBackupScheduler(), FakeBackupDataSource())

        useCase()

        assertTrue(prefs.clearCalled)
    }

    @Test
    fun `disconnect cancels scheduler`() = runTest {
        val scheduler = FakeBackupScheduler()
        val useCase = DisconnectBackupUseCase(buildRegistry(), FakeBackupPreferences(), scheduler, FakeBackupDataSource())

        useCase()

        assertTrue(scheduler.cancelAllCalled)
    }

    @Test
    fun `disconnect succeeds even when no provider is set`() = runTest {
        val prefs = FakeBackupPreferences() // backupProvider = null
        val useCase = DisconnectBackupUseCase(buildRegistry(), prefs, FakeBackupScheduler(), FakeBackupDataSource())

        val result = useCase()

        assertTrue(result is BackupResult.Success)
    }
}
