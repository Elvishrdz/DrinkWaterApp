package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.json.JsonObject

data class BackupMigration(
    val from: Int,
    val to: Int,
    val migrate: (JsonObject) -> JsonObject,
)
