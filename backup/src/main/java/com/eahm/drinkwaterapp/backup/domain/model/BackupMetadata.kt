package com.eahm.drinkwaterapp.backup.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Metadata block of a backup file. As of schema v3 it carries only the file-level descriptors:
 * the schema version and the creation / last-update timestamps. The device-specific upload
 * details (app version, device id, provider) now live in the sibling `last_upload_data` node —
 * see the serializer. Fields removed in v3 (`app_version`, `device_id`) are simply ignored when
 * decoding older files thanks to `ignoreUnknownKeys`.
 */
@Serializable
data class BackupMetadata(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("last_updated_at") val lastUpdatedAt: Long = 0L,
)
