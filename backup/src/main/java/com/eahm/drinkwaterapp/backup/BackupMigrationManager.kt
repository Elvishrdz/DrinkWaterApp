package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.BackupMigrationManager.currentSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

object BackupMigrationManager {

    const val currentSchema: Int = 1

    /**
     * Registered migrations in ascending order.
     * Add new migrations here as the schema evolves.
     */
    private val migrations: List<BackupMigration> = emptyList()

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
