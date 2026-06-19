package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BackupMigrationManagerTest {

    @Test
    fun `currentSchema is 3`() {
        assertEquals(3, BackupMigrationManager.currentSchema)
    }

    @Test
    fun `migrate returns Success unchanged when fromSchema equals currentSchema`() {
        val json = buildJsonObject { put("data", "value") }
        val result = BackupMigrationManager.migrate(json, BackupMigrationManager.currentSchema)
        assertEquals(BackupResult.Success(json), result)
    }

    @Test
    fun `migrate returns IncompatibleVersion when fromSchema is greater than currentSchema`() {
        val futureSchema = BackupMigrationManager.currentSchema + 1
        val result = BackupMigrationManager.migrate(buildJsonObject {}, futureSchema)
        assertTrue(result is BackupResult.Failure)
        val error = result.error
        assertTrue(error is BackupError.IncompatibleVersion)
        assertEquals(futureSchema, error.remoteSchema)
    }

    @Test
    fun `migrate returns MigrationFailed when no migration path exists`() {
        // There is no 0 → 1 migration registered; migrating from schema 0 fails gracefully.
        val result = BackupMigrationManager.migrate(buildJsonObject {}, fromSchema = 0)
        assertTrue(result is BackupResult.Failure)
        assertTrue(result.error is BackupError.MigrationFailed)
    }

    // ── v2 → v3 ──────────────────────────────────────────────────────────────────

    @Test
    fun `migrate v2 to v3 stamps schema_version and keeps created_at, adds last_updated_at`() {
        val migrated = migrateOrFail(v2Envelope(), fromSchema = 2)
        val metadata = migrated["metadata"]!!.jsonObject

        assertEquals(3, metadata["schema_version"]!!.jsonPrimitive.int)
        assertEquals(1_700_000_000_000L, metadata["created_at"]!!.jsonPrimitive.longOrNull)
        assertEquals(1_700_000_000_000L, metadata["last_updated_at"]!!.jsonPrimitive.longOrNull)
        // The device-specific descriptors must no longer live in metadata.
        assertNull(metadata["app_version"])
        assertNull(metadata["device_id"])
    }

    @Test
    fun `migrate v2 to v3 moves app_version and device_id into last_upload_data`() {
        val migrated = migrateOrFail(v2Envelope(), fromSchema = 2)
        val lastUpload = migrated["last_upload_data"]!!.jsonObject

        assertEquals("1.2.3", lastUpload["app_version"]!!.jsonPrimitive.content)
        assertEquals("pixel-8", lastUpload["device_id"]!!.jsonPrimitive.content)
        // Provider is unknown for pre-v3 files.
        assertEquals("", lastUpload["provider"]!!.jsonPrimitive.content)
    }

    @Test
    fun `migrate v2 to v3 drops deprecated preferences`() {
        val prefs = migrateOrFail(v2Envelope(), fromSchema = 2)["data"]!!
            .jsonObject["preferences"]!!.jsonObject

        assertNull(prefs["time_format_preference"])
        assertNull(prefs["water_animation_enabled"])
        // Untouched preferences survive.
        assertEquals("Elvis", prefs["username"]!!.jsonPrimitive.content)
        assertEquals("ML", prefs["unit_preference"]!!.jsonPrimitive.content)
    }

    @Test
    fun `migrate v2 to v3 backfills active hours, interval and active days`() {
        val prefs = migrateOrFail(v2Envelope(), fromSchema = 2)["data"]!!
            .jsonObject["preferences"]!!.jsonObject

        val start = prefs["start_time"]!!.jsonObject
        assertEquals(10, start["hour"]!!.jsonPrimitive.int)
        assertEquals(0, start["minute"]!!.jsonPrimitive.int)

        val end = prefs["end_time"]!!.jsonObject
        assertEquals(16, end["hour"]!!.jsonPrimitive.int)
        assertEquals(0, end["minute"]!!.jsonPrimitive.int)

        val interval = prefs["interval"]!!.jsonObject
        assertEquals(0, interval["hour"]!!.jsonPrimitive.int)
        assertEquals(45, interval["minute"]!!.jsonPrimitive.int)

        val days = prefs["active_days"]!!.jsonArray.map { it.jsonPrimitive.int }
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), days)
    }

    @Test
    fun `migrate v2 to v3 preserves existing active hours instead of overwriting them`() {
        val envelope = v2Envelope {
            put("start_time", buildJsonObject { put("hour", 7); put("minute", 30) })
            put("active_days", buildJsonArray { listOf(1, 3, 5).forEach { add(it) } })
        }
        val prefs = migrateOrFail(envelope, fromSchema = 2)["data"]!!
            .jsonObject["preferences"]!!.jsonObject

        assertEquals(7, prefs["start_time"]!!.jsonObject["hour"]!!.jsonPrimitive.int)
        assertEquals(30, prefs["start_time"]!!.jsonObject["minute"]!!.jsonPrimitive.int)
        assertEquals(
            listOf(1, 3, 5),
            prefs["active_days"]!!.jsonArray.map { it.jsonPrimitive.int },
        )
        // The fields that were missing are still backfilled.
        assertEquals(16, prefs["end_time"]!!.jsonObject["hour"]!!.jsonPrimitive.int)
    }

    // ── v1 → v3 (chained) ────────────────────────────────────────────────────────

    @Test
    fun `migrate v1 to v3 chains both migrations`() {
        val v1 = buildJsonObject {
            put("metadata", buildJsonObject {
                put("schema_version", 1)
                put("app_version", "0.9.0")
                put("created_at", 42L)
                put("device_id", "nexus")
            })
            put("data", buildJsonObject {
                put("history", buildJsonArray {
                    add(buildJsonObject {
                        put("id", "h1"); put("amount", 250f); put("status", "IN_APP")
                        put("timestamp", "2026-01-01T08:00:00")
                    })
                })
                put("daily_goals", buildJsonArray { })
                put("preferences", buildJsonObject {
                    put("unit_preference", "ML")
                    put("time_format_preference", "SYSTEM")
                })
            })
        }

        val migrated = migrateOrFail(v1, fromSchema = 1)

        assertEquals(3, migrated["metadata"]!!.jsonObject["schema_version"]!!.jsonPrimitive.int)
        // v1 → v2 backfilled device_id on the history record…
        val history = migrated["data"]!!.jsonObject["history"]!!.jsonArray.first().jsonObject
        assertEquals("", history["device_id"]!!.jsonPrimitive.content)
        // …and v2 → v3 moved the upload descriptors + cleaned preferences.
        assertEquals(
            "nexus",
            migrated["last_upload_data"]!!.jsonObject["device_id"]!!.jsonPrimitive.content,
        )
        val prefs = migrated["data"]!!.jsonObject["preferences"]!!.jsonObject
        assertNull(prefs["time_format_preference"])
        assertFalse(prefs["active_days"]!!.jsonArray.isEmpty())
    }

    // ── helpers ────────────────────────────────────────────────────────────────────

    private fun migrateOrFail(json: JsonObject, fromSchema: Int): JsonObject {
        val result = BackupMigrationManager.migrate(json, fromSchema)
        assertTrue(result is BackupResult.Success, "expected success but was $result")
        return result.value
    }

    /** A representative schema-v2 envelope; [extraPrefs] can add/override preference fields. */
    private fun v2Envelope(extraPrefs: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit = {}) =
        buildJsonObject {
            put("metadata", buildJsonObject {
                put("schema_version", 2)
                put("app_version", "1.2.3")
                put("created_at", 1_700_000_000_000L)
                put("device_id", "pixel-8")
            })
            put("data", buildJsonObject {
                put("history", buildJsonArray { })
                put("daily_goals", buildJsonArray { })
                put("preferences", buildJsonObject {
                    put("username", "Elvis")
                    put("amount_per_drink", 250f)
                    put("unit_preference", "ML")
                    put("time_format_preference", "SYSTEM")
                    put("water_animation_enabled", true)
                    put("best_goal_streak", 7)
                    put("best_drink_streak", 12)
                    extraPrefs()
                })
            })
        }
}
