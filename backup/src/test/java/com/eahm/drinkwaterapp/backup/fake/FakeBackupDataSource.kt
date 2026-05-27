package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.BackupDataSource
import com.eahm.drinkwaterapp.backup.BackupResult
import kotlinx.serialization.json.JsonObject

class FakeBackupDataSource(
    private val serialized: String = "{}",
    private val importResult: BackupResult<Unit> = BackupResult.Success(Unit),
) : BackupDataSource {
    var serializeCallCount = 0
    var lastImported: JsonObject? = null

    override suspend fun serialize(): String {
        serializeCallCount++
        return serialized
    }

    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit> {
        lastImported = data
        return importResult
    }
}
