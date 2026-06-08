package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.BackupRepositoryImpl
import com.eahm.drinkwaterapp.backup.data.InitialSyncResolver
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.InitialConnectionSyncUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.fake.FakeBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.FakeBackupScheduler
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupRepositoryImplTest {

    private fun buildRepository(
        prefs: FakeBackupPreferences = FakeBackupPreferences(),
        googleProvider: FakeCloudBackupProvider = FakeCloudBackupProvider(CloudProviderType.GOOGLE_DRIVE),
        dropboxProvider: FakeCloudBackupProvider = FakeCloudBackupProvider(CloudProviderType.DROPBOX),
        dataSource: FakeBackupDataSource = FakeBackupDataSource(),
    ): BackupRepositoryImpl {
        val registry = BackupProviderRegistry(googleProvider, dropboxProvider)
        val scheduler = FakeBackupScheduler()
        val mutex = Mutex()
        val syncUseCase = SyncBackupUseCase(
            mutex = mutex,
            dataSource = dataSource,
            prefs = prefs,
            mergeEngine = MergeEngine(),
            conflictResolver = SyncConflictResolver(),
            recoveryResolver = BackupRecoveryResolver(),
        )
        val restoreUseCase = RestoreBackupUseCase(dataSource)
        val checkStatusUseCase = CheckBackupStatusUseCase()
        val disconnectUseCase = DisconnectBackupUseCase(registry, prefs, scheduler)
        val migrateUseCase = MigrateBackupUseCase(mutex, prefs)
        val initialConnectionSyncUseCase = InitialConnectionSyncUseCase(
            syncUseCase = syncUseCase,
            restoreUseCase = restoreUseCase,
            resolver = InitialSyncResolver(),
        )
        val connectUseCase = ConnectBackupUseCase(registry, prefs, initialConnectionSyncUseCase)

        return BackupRepositoryImpl(
            registry = registry,
            prefs = prefs,
            connectUseCase = connectUseCase,
            syncUseCase = syncUseCase,
            restoreUseCase = restoreUseCase,
            checkStatusUseCase = checkStatusUseCase,
            disconnectUseCase = disconnectUseCase,
            migrateUseCase = migrateUseCase,
        )
    }

    @Test
    fun `initial syncState is Idle`() {
        val repo = buildRepository()
        assertEquals(BackupSyncState.Idle, repo.syncState.value)
    }

    @Test
    fun `sync returns Failure(NoProviderSelected) when no provider in prefs`() = runTest {
        val repo = buildRepository(prefs = FakeBackupPreferences())
        val result = repo.sync()
        assertTrue(result is BackupResult.Failure)
        assertEquals(BackupError.NoProviderSelected, result.error)
    }

    @Test
    fun `sync succeeds when provider is set and upload works`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
        }
        val repo = buildRepository(prefs = prefs)
        val result = repo.sync()
        assertTrue(result is BackupResult.Success)
    }

    @Test
    fun `sync returns AlreadyInProgress when syncState is InProgress`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
        }
        val repo = buildRepository(prefs = prefs)
        // Force state to InProgress
        val field = repo::class.java.getDeclaredField("_syncState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(repo) as kotlinx.coroutines.flow.MutableStateFlow<BackupSyncState>
        stateFlow.value = BackupSyncState.InProgress

        val result = repo.sync()
        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.AlreadyInProgress, result.value)
    }

    @Test
    fun `syncState transitions to Success after successful sync`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
        }
        val repo = buildRepository(prefs = prefs)
        repo.sync()
        assertTrue(repo.syncState.value is BackupSyncState.Success)
    }

    @Test
    fun `syncState transitions to Error after failed sync`() = runTest {
        val prefs = FakeBackupPreferences().apply {
            backupProvider = CloudProviderType.GOOGLE_DRIVE.id
        }
        val failingProvider = FakeCloudBackupProvider(
            CloudProviderType.GOOGLE_DRIVE,
            uploadResult = BackupResult.Failure(BackupError.NetworkUnavailable),
        )
        val repo = buildRepository(prefs = prefs, googleProvider = failingProvider)
        repo.sync()
        assertTrue(repo.syncState.value is BackupSyncState.Error)
    }

    @Test
    fun `checkRemoteStatus returns Failure when no provider selected`() = runTest {
        val repo = buildRepository(prefs = FakeBackupPreferences())
        val result = repo.checkRemoteStatus()
        assertTrue(result is BackupResult.Failure)
        assertEquals(BackupError.NoProviderSelected, result.error)
    }
}
