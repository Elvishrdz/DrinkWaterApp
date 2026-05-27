package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupMigrationManagerTest {

    @Test
    fun `currentSchema is 1`() {
        assertEquals(1, BackupMigrationManager.currentSchema)
    }

    @Test
    fun `migrate returns Success unchanged when fromSchema equals currentSchema`() {
        val json = buildJsonObject { put("data", "value") }
        val result = BackupMigrationManager.migrate(json, BackupMigrationManager.currentSchema)
        assertEquals(BackupResult.Success(json), result)
    }

    @Test
    fun `migrate returns IncompatibleVersion when fromSchema is greater than currentSchema`() {
        val futureSchema = BackupMigrationManager.currentSchema + 1
        val result = BackupMigrationManager.migrate(buildJsonObject {}, futureSchema)
        assertTrue(result is BackupResult.Failure)
        val error = result.error
        assertTrue(error is BackupError.IncompatibleVersion)
        assertEquals(futureSchema, error.remoteSchema)
    }

    @Test
    fun `migrate returns MigrationFailed when no migration path exists`() {
        // No migrations are registered yet; migrating from an older schema fails gracefully
        val result = BackupMigrationManager.migrate(buildJsonObject {}, fromSchema = 0)
        assertTrue(result is BackupResult.Failure)
        assertTrue(result.error is BackupError.MigrationFailed)
    }
}
