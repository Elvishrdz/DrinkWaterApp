package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.json.JsonObject

data class BackupFile(
    val metadata: BackupMetadata,
    val rawData: JsonObject,
)
