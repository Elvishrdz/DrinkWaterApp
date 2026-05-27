package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.json.JsonObject

interface BackupDataSource {
    suspend fun serialize(): String
    suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit>
}
