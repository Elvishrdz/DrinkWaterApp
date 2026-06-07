package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.backup.BackupDataSource
import com.eahm.drinkwaterapp.backup.BackupError
import com.eahm.drinkwaterapp.backup.BackupResult
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class BackupSerializer(
    private val database: AppDatabase,
    private val prefs: PrefsStorage,
    private val json: Json,
) : BackupDataSource {

    override suspend fun serialize(): String {
        val historyRecords = database.getHistoryDao().getAll().first()
        val dailyGoals = database.getDailyGoalDao().getAllGoals().first()

        val historyJson = buildJsonArray {
            historyRecords.forEach { record ->
                add(buildJsonObject {
                    put("id", record.id)
                    put("amount", record.amount)
                    put("status", record.status.name)
                    put("timestamp", record.timestamp.toString())
                    put("device_id", record.deviceId)
                    put("updated_at", record.updatedAt)
                    if (record.deletedAt != null) put("deleted_at", record.deletedAt) else put("deleted_at", null as String?)
                })
            }
        }

        val goalsJson = buildJsonArray {
            dailyGoals.forEach { goal ->
                add(buildJsonObject {
                    put("id", goal.id)
                    put("effective_date", goal.effectiveDate.toString())
                    put("goal_ml", goal.goalMl)
                    put("device_id", goal.deviceId)
                    put("updated_at", goal.updatedAt)
                    if (goal.deletedAt != null) put("deleted_at", goal.deletedAt) else put("deleted_at", null as String?)
                })
            }
        }

        val prefsJson = buildJsonObject {
            put("username", prefs.getUsername().first())
            put("amount_per_drink", prefs.getAmountPerDrink().first())
            put("unit_preference", prefs.getUnitPreference().first().name)
            put("time_format_preference", prefs.getTimeFormatPreference().first().name)
            put("water_animation_enabled", prefs.getWaterAnimationEnabled().first())
            put("best_goal_streak", prefs.getBestGoalStreak().first())
            put("best_drink_streak", prefs.getBestDrinkStreak().first())
        }

        val root = buildJsonObject {
            put("history", historyJson)
            put("daily_goals", goalsJson)
            put("preferences", prefsJson)
        }

        return json.encodeToString(JsonObject.serializer(), root)
    }

    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit> {
        return try {
            // Wipe existing data
            database.getHistoryDao().deleteAll()
            database.getDailyGoalDao().deleteAll()

            // Re-insert history
            val historyArray = data["history"]?.jsonArray ?: return BackupResult.Success(Unit)
            val historyEntities = historyArray.map { element ->
                val obj = element.jsonObject
                com.eahm.drinkwaterapp.core.database.entity.HistoryEntity(
                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: generateId(),
                    amount = obj["amount"]?.jsonPrimitive?.double?.toFloat() ?: 0f,
                    status = com.eahm.drinkwaterapp.core.database.entity.Status.valueOf(
                        obj["status"]?.jsonPrimitive?.contentOrNull ?: "IN_APP"
                    ),
                    timestamp = kotlinx.datetime.LocalDateTime.parse(
                        obj["timestamp"]?.jsonPrimitive?.contentOrNull ?: ""
                    ),
                    deviceId = obj["device_id"]?.jsonPrimitive?.contentOrNull ?: "",
                    updatedAt = obj["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                    deletedAt = obj["deleted_at"]?.jsonPrimitive?.longOrNull,
                )
            }
            if (historyEntities.isNotEmpty()) {
                database.getHistoryDao().insertAll(*historyEntities.toTypedArray())
            }

            // Re-insert daily goals
            val goalsArray = data["daily_goals"]?.jsonArray ?: emptyList()
            goalsArray.forEach { element ->
                val obj = element.jsonObject
                val goal = com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity(
                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: generateId(),
                    effectiveDate = kotlinx.datetime.LocalDate.parse(
                        obj["effective_date"]?.jsonPrimitive?.contentOrNull ?: ""
                    ),
                    goalMl = obj["goal_ml"]?.jsonPrimitive?.double?.toFloat() ?: 500f,
                    deviceId = obj["device_id"]?.jsonPrimitive?.contentOrNull ?: "",
                    updatedAt = obj["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                    deletedAt = obj["deleted_at"]?.jsonPrimitive?.longOrNull,
                )
                database.getDailyGoalDao().insert(goal)
            }

            // Restore preferences
            val prefsObj = data["preferences"]?.jsonObject
            if (prefsObj != null) {
                prefsObj["username"]?.jsonPrimitive?.contentOrNull
                    ?.let { prefs.setUsername(it) }
                prefsObj["amount_per_drink"]?.jsonPrimitive?.double?.toFloat()
                    ?.let { prefs.setAmountPerDrink(it) }
                prefsObj["unit_preference"]?.jsonPrimitive?.contentOrNull
                    ?.let { name ->
                        runCatching {
                            com.eahm.drinkwaterapp.core.presentation.UnitPreference.valueOf(name)
                        }.getOrNull()?.let { prefs.setUnitPreference(it) }
                    }
                prefsObj["time_format_preference"]?.jsonPrimitive?.contentOrNull
                    ?.let { name ->
                        runCatching {
                            com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference.valueOf(
                                name
                            )
                        }.getOrNull()?.let { prefs.setTimeFormatPreference(it) }
                    }
                prefsObj["water_animation_enabled"]?.jsonPrimitive?.contentOrNull
                    ?.toBooleanStrictOrNull()
                    ?.let { prefs.setWaterAnimationEnabled(it) }
                prefsObj["best_goal_streak"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                    ?.let { prefs.setBestGoalStreak(it) }
                prefsObj["best_drink_streak"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                    ?.let { prefs.setBestDrinkStreak(it) }
            }

            BackupResult.Success(Unit)
        } catch (e: Exception) {
            BackupResult.Failure(BackupError.MigrationFailed(e))
        }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private fun generateId(): String = kotlin.uuid.Uuid.random().toString()
}
