package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.backup.BackupData
import com.eahm.drinkwaterapp.backup.BackupDataSource
import com.eahm.drinkwaterapp.backup.BackupError
import com.eahm.drinkwaterapp.backup.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.BackupPreferences
import com.eahm.drinkwaterapp.backup.BackupResult
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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

@OptIn(kotlin.time.ExperimentalTime::class)
class BackupSerializer(
    private val database: AppDatabase,
    private val prefs: PrefsStorage,
    private val backupPreferences: BackupPreferences,
    private val appInfo: AppInfoService,
    private val json: Json,
) : BackupDataSource {

    @Suppress("LongMethod")
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
                    if (record.deletedAt != null) {
                        put(
                            "deleted_at",
                            record.deletedAt
                        )
                    } else {
                        put("deleted_at", null as String?)
                    }
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
                    if (goal.deletedAt != null) {
                        put(
                            "deleted_at",
                            goal.deletedAt
                        )
                    } else {
                        put("deleted_at", null as String?)
                    }
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

        val dataJson = buildJsonObject {
            put("history", historyJson)
            put("daily_goals", goalsJson)
            put("preferences", prefsJson)
        }

        val metadataJson = buildJsonObject {
            put("schema_version", BackupMigrationManager.currentSchema)
            put("app_version", appInfo.versionName)
            put("created_at", System.currentTimeMillis())
            put("device_id", backupPreferences.getDeviceId())
        }

        val root = buildJsonObject {
            put("metadata", metadataJson)
            put("data", dataJson)
        }

        return json.encodeToString(JsonObject.serializer(), root)
    }

    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit> {
        return try {
            // Wipe existing data
            database.getHistoryDao().deleteAll()
            database.getDailyGoalDao().deleteAll()

            // Records live under the "data" envelope; fall back to root for legacy flat files.
            val dataBlock = data["data"]?.jsonObject ?: data

            // Re-insert history
            val historyArray = dataBlock["history"]?.jsonArray ?: return BackupResult.Success(Unit)
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
            val goalsArray = dataBlock["daily_goals"]?.jsonArray ?: emptyList()
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
            val prefsObj = dataBlock["preferences"]?.jsonObject
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

    override suspend fun findNearExpiryTombstones(
        warningThreshold: Long,
        expiryThreshold: Long
    ): Int {
        val history = database.getHistoryDao().getAll().first()
        val goals = database.getDailyGoalDao().getAllGoals().first()
        val historyNear = history.count { record ->
            record.deletedAt != null && record.deletedAt >= warningThreshold && record.deletedAt < expiryThreshold
        }
        val goalsNear = goals.count { goal ->
            goal.deletedAt != null && goal.deletedAt >= warningThreshold && goal.deletedAt < expiryThreshold
        }
        return historyNear + goalsNear
    }

    override suspend fun purgeExpiredTombstones(expiryThreshold: Long) {
        val history = database.getHistoryDao().getAll().first()
        val goals = database.getDailyGoalDao().getAllGoals().first()

        history.filter { it.deletedAt != null && it.deletedAt <= expiryThreshold }
            .forEach { database.getHistoryDao().delete(it) }

        goals.filter { it.deletedAt != null && it.deletedAt <= expiryThreshold }
            .forEach { database.getDailyGoalDao().delete(it) }
    }

    override suspend fun readLocalData(deviceId: String): BackupData {
        val historyRecords = database.getHistoryDao().getAll().first()
        val dailyGoals = database.getDailyGoalDao().getAllGoals().first()

        val history = historyRecords.map { entity ->
            com.eahm.drinkwaterapp.backup.HistoryRecord(
                id = entity.id,
                amount = entity.amount,
                status = entity.status.name,
                timestamp = entity.timestamp
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds(),
                deviceId = entity.deviceId.ifEmpty { deviceId },
                updatedAt = if (entity.updatedAt == 0L) entity.timestamp
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds() else entity.updatedAt,
                deletedAt = entity.deletedAt,
            )
        }

        val goals = dailyGoals.map { entity ->
            com.eahm.drinkwaterapp.backup.GoalRecord(
                id = entity.id,
                effectiveDate = entity.effectiveDate.toEpochDays().toInt(),
                goalMl = entity.goalMl,
                deviceId = entity.deviceId.ifEmpty { deviceId },
                updatedAt = entity.updatedAt,
                deletedAt = entity.deletedAt,
            )
        }

        val unitPref = prefs.getUnitPreference().first().name
        val timeFormat = prefs.getTimeFormatPreference().first().name
        val amountPerDrink = prefs.getAmountPerDrink().first()

        val prefsRecord = com.eahm.drinkwaterapp.backup.PreferencesRecord(
            unitPreference = unitPref,
            timeFormat = timeFormat,
            amountPerDrink = amountPerDrink,
            deviceId = deviceId,
            updatedAt = 0L,
        )

        return BackupData(history, goals, prefsRecord)
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun parseRemoteData(raw: JsonObject): BackupData? {
        return try {
            val dataBlock = raw["data"]?.jsonObject ?: return null

            val history = dataBlock["history"]?.jsonArray?.mapNotNull { element ->
                val obj = element.jsonObject
                val timestampStr =
                    obj["timestamp"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val timestamp = runCatching {
                    kotlinx.datetime.LocalDateTime.parse(timestampStr)
                        .toInstant(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                }.getOrNull() ?: 0L

                com.eahm.drinkwaterapp.backup.HistoryRecord(
                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    amount = obj["amount"]?.jsonPrimitive?.double?.toFloat() ?: 0f,
                    status = obj["status"]?.jsonPrimitive?.contentOrNull ?: "IN_APP",
                    timestamp = timestamp,
                    deviceId = obj["device_id"]?.jsonPrimitive?.contentOrNull ?: "",
                    updatedAt = obj["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                    deletedAt = obj["deleted_at"]?.jsonPrimitive?.longOrNull,
                )
            } ?: emptyList()

            val goals = dataBlock["daily_goals"]?.jsonArray?.mapNotNull { element ->
                val obj = element.jsonObject
                val dateStr =
                    obj["effective_date"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val epochDays = runCatching {
                    kotlinx.datetime.LocalDate.parse(dateStr).toEpochDays().toInt()
                }.getOrNull() ?: return@mapNotNull null

                com.eahm.drinkwaterapp.backup.GoalRecord(
                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    effectiveDate = epochDays,
                    goalMl = obj["goal_ml"]?.jsonPrimitive?.double?.toFloat() ?: 500f,
                    deviceId = obj["device_id"]?.jsonPrimitive?.contentOrNull ?: "",
                    updatedAt = obj["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                    deletedAt = obj["deleted_at"]?.jsonPrimitive?.longOrNull,
                )
            } ?: emptyList()

            val prefsObj = dataBlock["preferences"]?.jsonObject
            val prefsRecord = com.eahm.drinkwaterapp.backup.PreferencesRecord(
                unitPreference = prefsObj?.get("unit_preference")?.jsonPrimitive?.contentOrNull
                    ?: "ML",
                timeFormat = prefsObj?.get("time_format_preference")?.jsonPrimitive?.contentOrNull
                    ?: "SYSTEM",
                amountPerDrink = prefsObj?.get("amount_per_drink")?.jsonPrimitive?.double?.toFloat()
                    ?: 250f,
                deviceId = prefsObj?.get("device_id")?.jsonPrimitive?.contentOrNull ?: "",
                updatedAt = prefsObj?.get("updated_at")?.jsonPrimitive?.longOrNull ?: 0L,
            )

            BackupData(history, goals, prefsRecord)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun writeBackupData(data: BackupData): BackupResult<Unit> {
        return try {
            database.getHistoryDao().deleteAll()
            database.getDailyGoalDao().deleteAll()

            val historyEntities = data.history.map { record ->
                val localDateTime = Instant.fromEpochMilliseconds(record.timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                com.eahm.drinkwaterapp.core.database.entity.HistoryEntity(
                    id = record.id,
                    amount = record.amount,
                    status = runCatching {
                        com.eahm.drinkwaterapp.core.database.entity.Status.valueOf(record.status)
                    }.getOrElse { com.eahm.drinkwaterapp.core.database.entity.Status.IN_APP },
                    timestamp = localDateTime,
                    deviceId = record.deviceId,
                    updatedAt = record.updatedAt,
                    deletedAt = record.deletedAt,
                )
            }
            if (historyEntities.isNotEmpty()) {
                database.getHistoryDao().insertAll(*historyEntities.toTypedArray())
            }

            data.dailyGoals.forEach { record ->
                val localDate = kotlinx.datetime.LocalDate.fromEpochDays(record.effectiveDate)
                val goal = com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity(
                    id = record.id,
                    effectiveDate = localDate,
                    goalMl = record.goalMl,
                    deviceId = record.deviceId,
                    updatedAt = record.updatedAt,
                    deletedAt = record.deletedAt,
                )
                database.getDailyGoalDao().insert(goal)
            }

            val prefsRecord = data.preferences
            runCatching {
                com.eahm.drinkwaterapp.core.presentation.UnitPreference.valueOf(prefsRecord.unitPreference)
            }.getOrNull()?.let { prefs.setUnitPreference(it) }

            runCatching {
                com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference.valueOf(prefsRecord.timeFormat)
            }.getOrNull()?.let { prefs.setTimeFormatPreference(it) }

            prefs.setAmountPerDrink(prefsRecord.amountPerDrink)

            com.eahm.drinkwaterapp.backup.BackupResult.Success(Unit)
        } catch (e: Exception) {
            com.eahm.drinkwaterapp.backup.BackupResult.Failure(
                com.eahm.drinkwaterapp.backup.BackupError.MigrationFailed(e)
            )
        }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private fun generateId(): String = kotlin.uuid.Uuid.random().toString()
}
