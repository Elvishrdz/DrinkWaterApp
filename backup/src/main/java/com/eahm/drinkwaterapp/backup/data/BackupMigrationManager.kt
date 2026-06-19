package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager.currentSchema
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupMigration
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

object BackupMigrationManager {

    const val currentSchema: Int = 3

    // Canonical defaults used when an older backup didn't capture a v3 preference. They mirror
    // the app's own DataStore defaults so a migrated file looks like a default-config v3 file.
    private const val DEFAULT_START_HOUR = 10
    private const val DEFAULT_END_HOUR = 16
    private const val DEFAULT_INTERVAL_MINUTE = 45
    private val DEFAULT_ACTIVE_DAYS = (1..7).toList()

    /**
     * Registered migrations in ascending order.
     * Add new migrations here as the schema evolves.
     */
    private val migrations: List<BackupMigration> = listOf(
        BackupMigration(from = 1, to = 2) { json ->
            // Add device_id="" and updated_at=0 to all history records
            val history = json["data"]?.jsonObject?.get("history")?.jsonArray
            val goals = json["data"]?.jsonObject?.get("daily_goals")?.jsonArray

            val migratedHistory = buildJsonArray {
                history?.forEach { element ->
                    add(buildJsonObject {
                        element.jsonObject.forEach { (k, v) -> put(k, v) }
                        if (!element.jsonObject.containsKey("device_id")) put("device_id", "")
                        if (!element.jsonObject.containsKey("updated_at")) put("updated_at", 0L)
                        if (!element.jsonObject.containsKey("deleted_at")) put(
                            "deleted_at",
                            null as String?
                        )
                    })
                }
            }

            val migratedGoals = buildJsonArray {
                goals?.forEach { element ->
                    add(buildJsonObject {
                        element.jsonObject.forEach { (k, v) -> put(k, v) }
                        if (!element.jsonObject.containsKey("device_id")) {
                            put("device_id", "")
                        }

                        if (!element.jsonObject.containsKey("updated_at")) {
                            put("updated_at", 0L)
                        }

                        if (!element.jsonObject.containsKey("deleted_at")) {
                            put(
                                "deleted_at",
                                null as String?
                            )
                        }
                    })
                }
            }

            val oldData = json["data"]?.jsonObject ?: buildJsonObject {}
            val newData = buildJsonObject {
                oldData.forEach { (k, v) -> put(k, v) }
                put("history", migratedHistory)
                put("daily_goals", migratedGoals)
            }

            buildJsonObject {
                json.forEach { (k, v) -> put(k, v) }
                put("data", newData)
            }
        },
        // v2 → v3:
        //  • metadata loses app_version + device_id and gains last_updated_at;
        //  • those upload details move into the new sibling `last_upload_data` node
        //    (provider is unknown for pre-v3 files, so it's left blank);
        //  • preferences drop the no-longer-synced time_format_preference and
        //    water_animation_enabled, and gain start_time/end_time/interval/active_days
        //    backfilled with the app's default values.
        BackupMigration(from = 2, to = 3) { json ->
            val oldMetadata = json["metadata"]?.jsonObject ?: buildJsonObject {}
            val createdAt = oldMetadata["created_at"]?.jsonPrimitive?.longOrNull ?: 0L

            // metadata: schema_version is stamped by [migrate]; keep created_at and seed
            // last_updated_at from it (the original update time is unknown for old files).
            val newMetadata = buildJsonObject {
                put("created_at", createdAt)
                put("last_updated_at", createdAt)
            }

            val lastUploadData = buildJsonObject {
                put("app_version", oldMetadata["app_version"]?.jsonPrimitive?.contentOrNull ?: "")
                put("device_id", oldMetadata["device_id"]?.jsonPrimitive?.contentOrNull ?: "")
                put("provider", "")
            }

            val oldData = json["data"]?.jsonObject ?: buildJsonObject {}
            val oldPrefs = oldData["preferences"]?.jsonObject ?: buildJsonObject {}
            val newPrefs = buildJsonObject {
                oldPrefs.forEach { (k, v) ->
                    if (k != "time_format_preference" && k != "water_animation_enabled") put(k, v)
                }
                if (!oldPrefs.containsKey("start_time")) {
                    put("start_time", activeTime(DEFAULT_START_HOUR, 0))
                }
                if (!oldPrefs.containsKey("end_time")) {
                    put("end_time", activeTime(DEFAULT_END_HOUR, 0))
                }
                if (!oldPrefs.containsKey("interval")) {
                    put("interval", activeTime(0, DEFAULT_INTERVAL_MINUTE))
                }
                if (!oldPrefs.containsKey("active_days")) {
                    put("active_days", buildJsonArray { DEFAULT_ACTIVE_DAYS.forEach { add(it) } })
                }
            }

            val newData = buildJsonObject {
                oldData.forEach { (k, v) -> if (k != "preferences") put(k, v) }
                put("preferences", newPrefs)
            }

            buildJsonObject {
                put("metadata", newMetadata)
                put("last_upload_data", lastUploadData)
                put("data", newData)
            }
        },
    )

    /** Builds an `{ "hour": h, "minute": m }` object matching the ActiveTime wire shape. */
    private fun activeTime(hour: Int, minute: Int): JsonObject = buildJsonObject {
        put("hour", hour)
        put("minute", minute)
    }

    /**
     * Migrates [json] from [fromSchema] up to [currentSchema], applying each migration in order.
     * Returns the original object unchanged if fromSchema == currentSchema.
     */
    fun migrate(json: JsonObject, fromSchema: Int): BackupResult<JsonObject> {
        if (fromSchema == currentSchema) return BackupResult.Success(json)
        if (fromSchema > currentSchema) {
            return BackupResult.Failure(BackupError.IncompatibleVersion(fromSchema))
        }

        return runCatching {
            var result = json
            var current = fromSchema
            while (current < currentSchema) {
                val migration = migrations.firstOrNull { it.from == current }
                    ?: error("No migration found from schema $current")
                result = migration.migrate(result)
                current = migration.to
            }
            // Update the schema version in the migrated object
            val updatedMetadata = buildJsonObject {
                result["metadata"]?.jsonObject?.forEach { (k, v) -> put(k, v) }
                put("schema_version", currentSchema)
            }
            buildJsonObject {
                result.forEach { (k, v) -> put(k, v) }
                put("metadata", updatedMetadata)
            }
        }.fold(
            onSuccess = { BackupResult.Success(it) },
            onFailure = { BackupResult.Failure(BackupError.MigrationFailed(it)) },
        )
    }
}
