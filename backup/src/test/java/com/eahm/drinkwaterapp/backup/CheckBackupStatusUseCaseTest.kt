package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckBackupStatusUseCaseTest {

    private val useCase = CheckBackupStatusUseCase()

    private fun metadata(schema: Int = BackupMigrationManager.currentSchema) = BackupMetadata(
        schemaVersion = schema,
        createdAt = 0L,
        lastUpdatedAt = 0L,
    )

    @Test
    fun `returns Success(null) when provider has no remote file`() = runTest {
        val provider = FakeCloudBackupProvider(
            metadataResult = BackupResult.Success(null),
        )
        val result = useCase(provider)
        assertTrue(result is BackupResult.Success)
        assertNull(result.value)
    }

    @Test
    fun `returns RemoteBackupStatus with isCompatible true when schema matches`() = runTest {
        val provider = FakeCloudBackupProvider(
            metadataResult = BackupResult.Success(metadata(BackupMigrationManager.currentSchema)),
        )
        val result = useCase(provider)
        assertTrue(result is BackupResult.Success)
        val status = result.value!!
        assertTrue(status.isCompatible)
        assertEquals(BackupMigrationManager.currentSchema, status.metadata.schemaVersion)
    }

    @Test
    fun `returns RemoteBackupStatus with isCompatible false when remote schema is newer`() =
        runTest {
            val futureSchema = BackupMigrationManager.currentSchema + 1
            val provider = FakeCloudBackupProvider(
                metadataResult = BackupResult.Success(metadata(futureSchema)),
            )
            val result = useCase(provider)
            assertTrue(result is BackupResult.Success)
            val status = result.value!!
            assertTrue(!status.isCompatible)
        }

    @Test
    fun `propagates provider failure`() = runTest {
        val provider = FakeCloudBackupProvider(
            metadataResult = BackupResult.Failure(BackupError.NetworkUnavailable),
        )
        val result = useCase(provider)
        assertTrue(result is BackupResult.Failure)
        assertEquals(BackupError.NetworkUnavailable, result.error)
    }
}
