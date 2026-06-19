package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.BackupSyncLock
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.usecase.PurgeTombstonesUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.domain.model.TombstonePolicy
import com.eahm.drinkwaterapp.backup.fake.FakeBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncBackupUseCaseTest {

    private fun buildUseCase(
        dataSource: FakeBackupDataSource = FakeBackupDataSource(),
        prefs: FakeBackupPreferences = FakeBackupPreferences(),
        lock: BackupSyncLock = BackupSyncLock(),
        mergeEngine: MergeEngine = MergeEngine(),
        conflictResolver: SyncConflictResolver = SyncConflictResolver(),
        recoveryResolver: BackupRecoveryResolver = BackupRecoveryResolver(),
    ) = SyncBackupUseCase(
        lock = lock,
        dataSource = dataSource,
        prefs = prefs,
        mergeEngine = mergeEngine,
        conflictResolver = conflictResolver,
        recoveryResolver = recoveryResolver,
        purgeTombstonesUseCase = PurgeTombstonesUseCase(dataSource, prefs),
        tombstonePolicy = TombstonePolicy(),
    )

    @Test
    fun `upload succeeds when content hash is new`() = runTest {
        val prefs = FakeBackupPreferences().apply { backupContentHash = null }
        val dataSource = FakeBackupDataSource(serialized = """{"data":"hello"}""")
        val provider = FakeCloudBackupProvider()
        val useCase = buildUseCase(dataSource, prefs)

        val result = useCase(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.Uploaded, result.value)
        assertEquals(1, provider.uploadCallCount)
    }

    @Test
    fun `skips upload when content fingerprint is unchanged`() = runTest {
        val dataSource = FakeBackupDataSource()
        // Stored hash matches the data source's current content fingerprint → nothing to push.
        val prefs = FakeBackupPreferences().apply {
            backupContentHash = dataSource.localContentFingerprint()
        }
        val provider = FakeCloudBackupProvider()
        val useCase = buildUseCase(dataSource, prefs)

        val result = useCase(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.SkippedNoChanges, result.value)
        assertEquals(0, provider.uploadCallCount)
    }

    @Test
    fun `returns AlreadyInProgress when mutex is locked`() = runTest {
        val lock = BackupSyncLock().apply { mutex.tryLock() }
        val useCase = buildUseCase(lock = lock)
        val result = useCase(FakeCloudBackupProvider())
        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.AlreadyInProgress, result.value)
    }

    @Test
    fun `persists hash and timestamp on successful upload`() = runTest {
        val prefs = FakeBackupPreferences()
        val useCase = buildUseCase(prefs = prefs)
        useCase(FakeCloudBackupProvider())
        assertNotNull(prefs.backupContentHash)
        assertNotNull(prefs.backupLastSyncAt)
        assertNull(prefs.backupLastError)
    }

    @Test
    fun `persists error key on upload failure`() = runTest {
        val prefs = FakeBackupPreferences()
        val provider = FakeCloudBackupProvider(
            uploadResult = BackupResult.Failure(BackupError.UploadFailed()),
        )
        val useCase = buildUseCase(prefs = prefs)
        val result = useCase(provider)
        assertTrue(result is BackupResult.Failure)
        assertNotNull(prefs.backupLastError)
    }
}
