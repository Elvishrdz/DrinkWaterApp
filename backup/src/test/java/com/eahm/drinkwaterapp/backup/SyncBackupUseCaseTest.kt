package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.fake.FakeBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.sync.Mutex
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
        mutex: Mutex = Mutex(),
        mergeEngine: MergeEngine = MergeEngine(),
        conflictResolver: SyncConflictResolver = SyncConflictResolver(),
        recoveryResolver: BackupRecoveryResolver = BackupRecoveryResolver(),
    ) = SyncBackupUseCase(
        mutex = mutex,
        dataSource = dataSource,
        prefs = prefs,
        mergeEngine = mergeEngine,
        conflictResolver = conflictResolver,
        recoveryResolver = recoveryResolver,
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
    fun `skips upload when hash is unchanged`() = runTest {
        val serialized = """{"data":"same"}"""
        // Pre-compute the expected sha256 to set as stored hash
        val expectedHash = sha256(serialized)
        val prefs = FakeBackupPreferences().apply { backupContentHash = expectedHash }
        val dataSource = FakeBackupDataSource(serialized = serialized)
        val provider = FakeCloudBackupProvider()
        val useCase = buildUseCase(dataSource, prefs)

        val result = useCase(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.SkippedNoChanges, result.value)
        assertEquals(0, provider.uploadCallCount)
    }

    @Test
    fun `returns AlreadyInProgress when mutex is locked`() = runTest {
        val mutex = Mutex(locked = true)
        val useCase = buildUseCase(mutex = mutex)
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

    // Replicate the SHA-256 logic from SyncBackupUseCase to compute expected hashes in tests
    private fun sha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
