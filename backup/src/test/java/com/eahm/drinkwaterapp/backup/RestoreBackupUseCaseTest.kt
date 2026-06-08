package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupFile
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.fake.FakeBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.FakeCloudBackupProvider
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestoreBackupUseCaseTest {

    private fun buildUseCase(dataSource: FakeBackupDataSource = FakeBackupDataSource()) =
        RestoreBackupUseCase(dataSource)

    private fun metadataWithSchema(schema: Int) = BackupMetadata(
        schemaVersion = schema,
        appVersion = "1.0",
        createdAt = 0L,
        deviceId = "test",
    )

    @Test
    fun `restore succeeds when remote schema matches current`() = runTest {
        val provider = FakeCloudBackupProvider(
            downloadResult = BackupResult.Success(
                BackupFile(
                    metadata = metadataWithSchema(BackupMigrationManager.currentSchema),
                    rawData = buildJsonObject {},
                )
            )
        )
        val result = buildUseCase().invoke(provider)
        assertTrue(result is BackupResult.Success)
    }

    @Test
    fun `restore fails with IncompatibleVersion when remote schema is newer`() = runTest {
        val futureSchema = BackupMigrationManager.currentSchema + 1
        val provider = FakeCloudBackupProvider(
            downloadResult = BackupResult.Success(
                BackupFile(
                    metadata = metadataWithSchema(futureSchema),
                    rawData = buildJsonObject {},
                )
            )
        )
        val result = buildUseCase().invoke(provider)
        assertTrue(result is BackupResult.Failure)
        val error = result.error
        assertTrue(error is BackupError.IncompatibleVersion)
        assertEquals(futureSchema, error.remoteSchema)
    }

    @Test
    fun `restore propagates download failure`() = runTest {
        val provider = FakeCloudBackupProvider(
            downloadResult = BackupResult.Failure(BackupError.DownloadFailed()),
        )
        val result = buildUseCase().invoke(provider)
        assertTrue(result is BackupResult.Failure)
        assertTrue(result.error is BackupError.DownloadFailed)
    }

    @Test
    fun `restore calls deserializeAndImport with downloaded data`() = runTest {
        val rawData = buildJsonObject { }
        val dataSource = FakeBackupDataSource()
        val provider = FakeCloudBackupProvider(
            downloadResult = BackupResult.Success(
                BackupFile(
                    metadata = metadataWithSchema(BackupMigrationManager.currentSchema),
                    rawData = rawData,
                )
            )
        )
        buildUseCase(dataSource).invoke(provider)
        assertEquals(rawData, dataSource.lastImported)
    }

    @Test
    fun `restore propagates deserializeAndImport failure`() = runTest {
        val dataSource = FakeBackupDataSource(
            importResult = BackupResult.Failure(BackupError.MigrationFailed()),
        )
        val provider = FakeCloudBackupProvider(
            downloadResult = BackupResult.Success(
                BackupFile(
                    metadata = metadataWithSchema(BackupMigrationManager.currentSchema),
                    rawData = buildJsonObject {},
                )
            )
        )
        val result = buildUseCase(dataSource).invoke(provider)
        assertTrue(result is BackupResult.Failure)
        assertTrue(result.error is BackupError.MigrationFailed)
    }
}
