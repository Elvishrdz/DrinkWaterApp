package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("app_version") val appVersion: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("device_id") val deviceId: String = "",
)
