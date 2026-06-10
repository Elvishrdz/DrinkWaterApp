package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.HistoryRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * Test double that behaves like the real Room-backed [BackupDataSource] at the data level:
 * it holds an in-memory [BackupData] snapshot and round-trips it through the same
 * `{ metadata, data: { history, daily_goals, preferences } }` envelope shape the production
 * serializer uses, so the real [com.eahm.drinkwaterapp.backup.data.MergeEngine] and
 * [com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase] run end-to-end.
 *
 * Timestamps are encoded as plain epoch-millis longs (vs the production LocalDateTime strings) —
 * this double is self-consistent so the exact wire encoding is irrelevant; the production wire
 * format is covered separately by the Robolectric round-trip tests.
 */
class InMemoryBackupDataSource(
    initial: BackupData = emptyBackup(),
) : BackupDataSource {

    var local: BackupData = initial
    var writeCallCount = 0
    var serializeCallCount = 0

    override suspend fun serialize(): String {
        serializeCallCount++
        return Json.encodeToString(JsonObject.serializer(), envelope(local))
    }

    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit> {
        local = decode(data["data"]?.jsonObject ?: data)
        return BackupResult.Success(Unit)
    }

    override suspend fun findNearExpiryTombstones(
        warningThreshold: Long,
        expiryThreshold: Long,
    ): Int = 0

    override suspend fun purgeExpiredTombstones(expiryThreshold: Long) {
        local = local.copy(
            history = local.history.filter { it.deletedAt == null || it.deletedAt > expiryThreshold },
            dailyGoals = local.dailyGoals.filter {
                it.deletedAt == null || it.deletedAt > expiryThreshold
            },
        )
    }

    override suspend fun readLocalData(deviceId: String): BackupData = local.copy(
        history = local.history.map {
            if (it.deviceId.isBlank()) it.copy(deviceId = deviceId) else it
        },
        dailyGoals = local.dailyGoals.map {
            if (it.deviceId.isBlank()) it.copy(deviceId = deviceId) else it
        },
    )

    override suspend fun parseRemoteData(raw: JsonObject): BackupData? =
        raw["data"]?.jsonObject?.let { decode(raw) }

    override suspend fun writeBackupData(data: BackupData): BackupResult<Unit> {
        writeCallCount++
        local = data
        return BackupResult.Success(Unit)
    }

    override suspend fun localContentFingerprint(): String = fingerprint(local)

    override fun remoteContentFingerprint(raw: JsonObject): String = fingerprint(decode(raw))

    // ── JSON envelope helpers (mirror the production field names) ──────────────────────────

    private fun envelope(data: BackupData): JsonObject = buildJsonObject {
        put(
            "metadata",
            buildJsonObject {
                put("schema_version", BackupMigrationManager.currentSchema)
                put("device_id", data.preferences.deviceId)
            },
        )
        put("data", encodeData(data))
    }

    private fun encodeData(data: BackupData): JsonObject = buildJsonObject {
        put(
            "history",
            buildJsonArray {
                data.history.forEach { r ->
                    add(
                        buildJsonObject {
                            put("id", r.id)
                            put("amount", r.amount)
                            put("status", r.status)
                            put("timestamp", r.timestamp)
                            put("device_id", r.deviceId)
                            put("updated_at", r.updatedAt)
                            put("deleted_at", r.deletedAt)
                        },
                    )
                }
            },
        )
        put(
            "daily_goals",
            buildJsonArray {
                data.dailyGoals.forEach { g ->
                    add(
                        buildJsonObject {
                            put("id", g.id)
                            put("effective_date", g.effectiveDate)
                            put("goal_ml", g.goalMl)
                            put("device_id", g.deviceId)
                            put("updated_at", g.updatedAt)
                            put("deleted_at", g.deletedAt)
                        },
                    )
                }
            },
        )
        put(
            "preferences",
            buildJsonObject {
                put("unit_preference", data.preferences.unitPreference)
                put("time_format_preference", data.preferences.timeFormat)
                put("amount_per_drink", data.preferences.amountPerDrink)
                put("device_id", data.preferences.deviceId)
                put("updated_at", data.preferences.updatedAt)
            },
        )
    }

    private fun decode(envelope: JsonObject): BackupData {
        val dataBlock = envelope["data"]?.jsonObject ?: envelope
        val history = dataBlock["history"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            HistoryRecord(
                id = o["id"]!!.jsonPrimitive.content,
                amount = o["amount"]?.jsonPrimitive?.double?.toFloat() ?: 0f,
                status = o["status"]?.jsonPrimitive?.content ?: "IN_APP",
                timestamp = o["timestamp"]?.jsonPrimitive?.longOrNull ?: 0L,
                deviceId = o["device_id"]?.jsonPrimitive?.content ?: "",
                updatedAt = o["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                deletedAt = o["deleted_at"]?.jsonPrimitive?.longOrNull,
            )
        } ?: emptyList()
        val goals = dataBlock["daily_goals"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            GoalRecord(
                id = o["id"]!!.jsonPrimitive.content,
                effectiveDate = (o["effective_date"]?.jsonPrimitive?.longOrNull ?: 0L).toInt(),
                goalMl = o["goal_ml"]?.jsonPrimitive?.double?.toFloat() ?: 0f,
                deviceId = o["device_id"]?.jsonPrimitive?.content ?: "",
                updatedAt = o["updated_at"]?.jsonPrimitive?.longOrNull ?: 0L,
                deletedAt = o["deleted_at"]?.jsonPrimitive?.longOrNull,
            )
        } ?: emptyList()
        val p = dataBlock["preferences"]?.jsonObject
        val prefs = PreferencesRecord(
            unitPreference = p?.get("unit_preference")?.jsonPrimitive?.content ?: "ML",
            timeFormat = p?.get("time_format_preference")?.jsonPrimitive?.content ?: "SYSTEM",
            amountPerDrink = p?.get("amount_per_drink")?.jsonPrimitive?.double?.toFloat() ?: 250f,
            deviceId = p?.get("device_id")?.jsonPrimitive?.content ?: "",
            updatedAt = p?.get("updated_at")?.jsonPrimitive?.longOrNull ?: 0L,
        )
        return BackupData(history, goals, prefs)
    }

    /** Order-independent content fingerprint, comparable between local and remote. */
    private fun fingerprint(data: BackupData): String {
        val h = data.history.sortedBy { it.id }
            .joinToString("|") { "${it.id},${it.amount},${it.status},${it.timestamp},${it.deletedAt}" }
        val g = data.dailyGoals.sortedBy { it.id }
            .joinToString("|") { "${it.id},${it.effectiveDate},${it.goalMl},${it.deletedAt}" }
        val p = with(data.preferences) { "$unitPreference,$timeFormat,$amountPerDrink" }
        return "$h#$g#$p"
    }

    companion object {
        fun emptyBackup(): BackupData = BackupData(
            history = emptyList(),
            dailyGoals = emptyList(),
            preferences = PreferencesRecord("ML", "SYSTEM", 250f),
        )
    }
}
