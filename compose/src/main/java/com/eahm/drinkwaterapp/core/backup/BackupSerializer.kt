package com.eahm.drinkwaterapp.core.backup

import com.eahm.drinkwaterapp.backup.data.BackupContentFingerprint
import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.HistoryRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.intOrNull
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

    override suspend fun serialize(createdAt: Long?): String {
        val now = System.currentTimeMillis()
        // created_at is preserved from the existing remote file (passed in) and only stamped
        // fresh when creating/replacing a file; last_updated_at always reflects this write.
        val effectiveCreatedAt = createdAt?.takeIf { it > 0L } ?: now

        val metadataJson = buildJsonObject {
            put("schema_version", BackupMigrationManager.currentSchema)
            put("created_at", effectiveCreatedAt)
            put("last_updated_at", now)
        }

        // Details of the device performing this upload. `provider` records where the backup
        // currently lives (google_drive / dropbox).
        val lastUploadJson = buildJsonObject {
            put("app_version", appInfo.versionName)
            put("device_id", backupPreferences.getDeviceId())
            put("provider", backupPreferences.getBackupProvider().orEmpty())
        }

        val root = buildJsonObject {
            put("metadata", metadataJson)
            put("last_upload_data", lastUploadJson)
            put("data", buildDataJson())
        }

        return json.encodeToString(JsonObject.serializer(), root)
    }

    /** Builds an `{ "hour": h, "minute": m }` object matching the ActiveTime wire shape. */
    private fun activeTimeJson(time: ActiveTime): JsonObject = buildJsonObject {
        put("hour", time.hour)
        put("minute", time.minute)
    }

    private fun parseActiveTime(obj: JsonObject): ActiveTime? {
        val hour = obj["hour"]?.jsonPrimitive?.intOrNull ?: return null
        val minute = obj["minute"]?.jsonPrimitive?.intOrNull ?: return null
        return ActiveTime(hour, minute)
    }

    /** Builds the data envelope (history + goals + preferences), excluding metadata. */
    @Suppress("LongMethod")
    private suspend fun buildDataJson(): JsonObject {
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
            // Active hours + notification cadence (time format & water animation are device-local
            // and intentionally NOT backed up as of schema v3).
            put("start_time", activeTimeJson(prefs.getStartTime().first()))
            put("end_time", activeTimeJson(prefs.getEndTime().first()))
            put("interval", activeTimeJson(prefs.getInterval().first()))
            put(
                "active_days",
                buildJsonArray { prefs.getActiveDays().first().sorted().forEach { add(it) } },
            )
            put("best_goal_streak", prefs.getBestGoalStreak().first())
            put("best_drink_streak", prefs.getBestDrinkStreak().first())
        }

        return buildJsonObject {
            put("history", historyJson)
            put("daily_goals", goalsJson)
            put("preferences", prefsJson)
        }
    }

    override suspend fun localContentFingerprint(): String =
        BackupContentFingerprint.ofData(buildDataJson())

    override fun remoteContentFingerprint(raw: JsonObject): String =
        BackupContentFingerprint.ofRemoteEnvelope(raw)

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
                prefsObj["start_time"]?.jsonObject?.let { parseActiveTime(it) }
                    ?.let { prefs.setStartTime(it) }
                prefsObj["end_time"]?.jsonObject?.let { parseActiveTime(it) }
                    ?.let { prefs.setEndTime(it) }
                prefsObj["interval"]?.jsonObject?.let { parseActiveTime(it) }
                    ?.let { prefs.setInterval(it) }
                prefsObj["active_days"]?.jsonArray
                    ?.mapNotNull { it.jsonPrimitive.intOrNull }?.toSet()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { prefs.setActiveDays(it) }
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
            HistoryRecord(
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
            GoalRecord(
                id = entity.id,
                effectiveDate = entity.effectiveDate.toEpochDays().toInt(),
                goalMl = entity.goalMl,
                deviceId = entity.deviceId.ifEmpty { deviceId },
                updatedAt = entity.updatedAt,
                deletedAt = entity.deletedAt,
            )
        }

        val unitPref = prefs.getUnitPreference().first().name
        val amountPerDrink = prefs.getAmountPerDrink().first()

        val prefsRecord = PreferencesRecord(
            unitPreference = unitPref,
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

                HistoryRecord(
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

                GoalRecord(
                    id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    effectiveDate = epochDays,
                    goalMl = obj["goal_ml"]?.jsonPrimitive?.double?.toFloat() ?: 500f,
                    deviceId = obj["device_id"]?.jsonPrimitive?.contentOrNull ?: "",
                    updatedAt = obj["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                    deletedAt = obj["deleted_at"]?.jsonPrimitive?.longOrNull,
                )
            } ?: emptyList()

            val prefsObj = dataBlock["preferences"]?.jsonObject
            val prefsRecord = PreferencesRecord(
                unitPreference = prefsObj?.get("unit_preference")?.jsonPrimitive?.contentOrNull
                    ?: "ML",
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

            prefs.setAmountPerDrink(prefsRecord.amountPerDrink)

            BackupResult.Success(Unit)
        } catch (e: Exception) {
            BackupResult.Failure(
                BackupError.MigrationFailed(e)
            )
        }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private fun generateId(): String = kotlin.uuid.Uuid.random().toString()
}
