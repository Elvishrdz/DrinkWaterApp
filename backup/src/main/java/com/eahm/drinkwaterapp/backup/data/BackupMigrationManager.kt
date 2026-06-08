package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager.currentSchema
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupMigration
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

object BackupMigrationManager {

    const val currentSchema: Int = 2

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
    )

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
