package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupContentFingerprint
import com.eahm.drinkwaterapp.backup.data.BackupMigrationManager
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Exhaustive tests for the local/remote content comparison ([BackupContentFingerprint]).
 *
 * The comparison must be:
 *  - order-independent (records and object keys may appear in any order),
 *  - metadata-independent (created_at / device_id / app_version of the file are ignored),
 *  - exact (any change to any content field flips the fingerprint),
 *  - stable for a given schema version (locked by a golden test).
 */
class BackupContentFingerprintTest {

    private val json = Json

    // ── Builders mirroring BackupSerializer's data shape (schema v2) ──────────────

    private fun history(
        id: String,
        amount: Float,
        status: String = "IN_APP",
        timestamp: String,
        deviceId: String = "device-a",
        updatedAt: Long = 0L,
        deletedAt: Long? = null,
    ) = buildJsonObject {
        put("id", id)
        put("amount", amount)
        put("status", status)
        put("timestamp", timestamp)
        put("device_id", deviceId)
        put("updated_at", updatedAt)
        if (deletedAt != null) put("deleted_at", deletedAt) else put("deleted_at", null as String?)
    }

    private fun goal(
        id: String,
        effectiveDate: String,
        goalMl: Float,
        deviceId: String = "device-a",
        updatedAt: Long = 0L,
        deletedAt: Long? = null,
    ) = buildJsonObject {
        put("id", id)
        put("effective_date", effectiveDate)
        put("goal_ml", goalMl)
        put("device_id", deviceId)
        put("updated_at", updatedAt)
        if (deletedAt != null) put("deleted_at", deletedAt) else put("deleted_at", null as String?)
    }

    private fun prefs(
        username: String = "Elvis",
        amountPerDrink: Float = 250f,
        unit: String = "ML",
        timeFormat: String = "SYSTEM",
        waterAnimation: Boolean = true,
        bestGoalStreak: Int = 7,
        bestDrinkStreak: Int = 12,
    ) = buildJsonObject {
        put("username", username)
        put("amount_per_drink", amountPerDrink)
        put("unit_preference", unit)
        put("time_format_preference", timeFormat)
        put("water_animation_enabled", waterAnimation)
        put("best_goal_streak", bestGoalStreak)
        put("best_drink_streak", bestDrinkStreak)
    }

    private fun activeTime(hour: Int, minute: Int) = buildJsonObject {
        put("hour", hour)
        put("minute", minute)
    }

    /** Preferences in the schema-v3 shape: no time format / water animation, plus active hours. */
    private fun prefsV3(
        username: String = "Elvis",
        amountPerDrink: Float = 250f,
        unit: String = "ML",
        startHour: Int = 10,
        endHour: Int = 16,
        intervalMinute: Int = 45,
        activeDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
        bestGoalStreak: Int = 7,
        bestDrinkStreak: Int = 12,
    ) = buildJsonObject {
        put("username", username)
        put("amount_per_drink", amountPerDrink)
        put("unit_preference", unit)
        put("start_time", activeTime(startHour, 0))
        put("end_time", activeTime(endHour, 0))
        put("interval", activeTime(0, intervalMinute))
        put("active_days", buildJsonArray { activeDays.forEach { add(it) } })
        put("best_goal_streak", bestGoalStreak)
        put("best_drink_streak", bestDrinkStreak)
    }

    private fun dataBlock(
        history: List<JsonObject>,
        goals: List<JsonObject>,
        preferences: JsonObject,
    ) = buildJsonObject {
        put("history", buildJsonArray { history.forEach { add(it) } })
        put("daily_goals", buildJsonArray { goals.forEach { add(it) } })
        put("preferences", preferences)
    }

    private fun envelope(
        data: JsonObject,
        schemaVersion: Int = 2,
        appVersion: String = "1.0.0",
        createdAt: Long = 1_700_000_000_000L,
        deviceId: String = "device-a",
    ) = buildJsonObject {
        put("metadata", buildJsonObject {
            put("schema_version", schemaVersion)
            put("app_version", appVersion)
            put("created_at", createdAt)
            put("device_id", deviceId)
        })
        put("data", data)
    }

    /** A deliberately complex, deterministic dataset (no randomness) used across tests. */
    private fun complexData(): JsonObject {
        val history = (1..50).map { i ->
            history(
                id = "h-%03d".format(i),
                amount = (100 + i * 7).toFloat(),
                status = if (i % 3 == 0) "FROM_NOTIFICATION" else "IN_APP",
                timestamp = "2026-0${(i % 9) + 1}-15T0$i:30:00".take(19),
                deviceId = if (i % 2 == 0) "device-a" else "device-b",
                updatedAt = 1_700_000_000_000L + i,
                deletedAt = if (i % 10 == 0) 1_700_000_500_000L + i else null,
            )
        }
        val goals = (1..15).map { i ->
            goal(
                id = "g-%02d".format(i),
                effectiveDate = "2026-01-%02d".format(i),
                goalMl = (2000 + i * 50).toFloat(),
                deviceId = if (i % 2 == 0) "device-b" else "device-a",
                updatedAt = 1_700_000_100_000L + i,
                deletedAt = if (i % 7 == 0) 1_700_000_600_000L + i else null,
            )
        }
        return dataBlock(history, goals, prefs())
    }

    private fun reparse(obj: JsonObject): JsonObject =
        json.parseToJsonElement(json.encodeToString(JsonObject.serializer(), obj)) as JsonObject

    // ── Equality: identical content is equal regardless of ordering ──────────────

    @Test
    fun `identical data is equal`() {
        val a = complexData()
        val b = complexData()
        assertEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `record order does not affect fingerprint`() {
        val h = (1..20).map {
            history(
                id = "h$it",
                amount = it.toFloat(),
                timestamp = "2026-01-01T00:00:0${it % 10}"
            )
        }
        val g = (1..8).map {
            goal(
                id = "g$it",
                effectiveDate = "2026-01-0$it",
                goalMl = (it * 100).toFloat()
            )
        }

        val ordered = dataBlock(h, g, prefs())
        val shuffled = dataBlock(h.reversed(), g.shuffled(java.util.Random(42)), prefs())

        assertEquals(
            BackupContentFingerprint.ofData(ordered),
            BackupContentFingerprint.ofData(shuffled),
        )
    }

    @Test
    fun `object key order does not affect fingerprint`() {
        val normal = buildJsonObject {
            put("id", "h1"); put("amount", 250f); put("status", "IN_APP")
            put("timestamp", "2026-01-01T08:00:00"); put("device_id", "device-a")
            put("updated_at", 10L); put("deleted_at", null as String?)
        }
        val reordered = buildJsonObject {
            put("deleted_at", null as String?); put("updated_at", 10L)
            put("device_id", "device-a"); put("timestamp", "2026-01-01T08:00:00")
            put("status", "IN_APP"); put("amount", 250f); put("id", "h1")
        }
        val a = dataBlock(listOf(normal), emptyList(), prefs())
        val b = dataBlock(listOf(reordered), emptyList(), prefs())
        assertEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `metadata differences are ignored`() {
        val data = complexData()
        val envA = envelope(data, appVersion = "1.0.0", createdAt = 1L, deviceId = "device-a")
        val envB = envelope(data, appVersion = "9.9.9", createdAt = 999L, deviceId = "device-z")
        assertEquals(
            BackupContentFingerprint.ofRemoteEnvelope(envA),
            BackupContentFingerprint.ofRemoteEnvelope(envB),
        )
    }

    @Test
    fun `local data block matches remote envelope round-tripped through JSON string`() {
        // Simulates: local builds a data block; remote is the same data downloaded as text.
        val data = complexData()
        val localFp = BackupContentFingerprint.ofData(data)
        val remoteFp = BackupContentFingerprint.ofRemoteEnvelope(reparse(envelope(data)))
        assertEquals(localFp, remoteFp)
    }

    @Test
    fun `legacy flat file (no data wrapper) falls back to root`() {
        val data = complexData()
        assertEquals(
            BackupContentFingerprint.ofData(data),
            BackupContentFingerprint.ofRemoteEnvelope(data),
        )
    }

    // ── Difference detection: any content change flips the fingerprint ───────────

    @Test
    fun `changing a history amount changes the fingerprint`() {
        val base = complexData()
        val changed = dataBlock(
            history = listOf(
                history(
                    id = "h-001",
                    amount = 999f,
                    timestamp = "2026-01-15T01:30:00"
                )
            ) +
                    (2..50).map {
                        history(
                            id = "h-%03d".format(it),
                            amount = (100 + it * 7).toFloat(),
                            timestamp = "x"
                        )
                    },
            goals = emptyList(),
            preferences = prefs(),
        )
        assertNotEquals(
            BackupContentFingerprint.ofData(base),
            BackupContentFingerprint.ofData(changed)
        )
    }

    @Test
    fun `deleted_at null vs value changes the fingerprint`() {
        val notDeleted = dataBlock(
            listOf(
                history(
                    id = "h1",
                    amount = 250f,
                    timestamp = "2026-01-01T08:00:00",
                    deletedAt = null
                )
            ),
            emptyList(), prefs(),
        )
        val deleted = dataBlock(
            listOf(
                history(
                    id = "h1",
                    amount = 250f,
                    timestamp = "2026-01-01T08:00:00",
                    deletedAt = 1L
                )
            ),
            emptyList(), prefs(),
        )
        assertNotEquals(
            BackupContentFingerprint.ofData(notDeleted),
            BackupContentFingerprint.ofData(deleted)
        )
    }

    @Test
    fun `changing a preference changes the fingerprint`() {
        val a = dataBlock(emptyList(), emptyList(), prefs(unit = "ML"))
        val b = dataBlock(emptyList(), emptyList(), prefs(unit = "OZ"))
        assertNotEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `changing username changes the fingerprint`() {
        val a = dataBlock(emptyList(), emptyList(), prefs(username = "Elvis"))
        val b = dataBlock(emptyList(), emptyList(), prefs(username = "Maria"))
        assertNotEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `adding a record changes the fingerprint`() {
        val base = complexData()
        val withExtra = dataBlock(
            history = (1..51).map {
                history(
                    id = "h-%03d".format(it),
                    amount = it.toFloat(),
                    timestamp = "t"
                )
            },
            goals = emptyList(),
            preferences = prefs(),
        )
        assertNotEquals(
            BackupContentFingerprint.ofData(base),
            BackupContentFingerprint.ofData(withExtra)
        )
    }

    @Test
    fun `changing a goal value changes the fingerprint`() {
        val a = dataBlock(
            emptyList(),
            listOf(goal(id = "g1", effectiveDate = "2026-01-01", goalMl = 2000f)),
            prefs()
        )
        val b = dataBlock(
            emptyList(),
            listOf(goal(id = "g1", effectiveDate = "2026-01-01", goalMl = 2500f)),
            prefs()
        )
        assertNotEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `two records swapping ids are not equal`() {
        // Same field values but ids swapped → different content, must differ.
        val a = dataBlock(
            listOf(
                history(id = "h1", amount = 100f, timestamp = "2026-01-01T01:00:00"),
                history(id = "h2", amount = 200f, timestamp = "2026-01-01T02:00:00"),
            ),
            emptyList(), prefs(),
        )
        val b = dataBlock(
            listOf(
                history(id = "h2", amount = 100f, timestamp = "2026-01-01T01:00:00"),
                history(id = "h1", amount = 200f, timestamp = "2026-01-01T02:00:00"),
            ),
            emptyList(), prefs(),
        )
        assertNotEquals(BackupContentFingerprint.ofData(a), BackupContentFingerprint.ofData(b))
    }

    @Test
    fun `empty database has a stable fingerprint distinct from non-empty`() {
        val empty = dataBlock(emptyList(), emptyList(), prefs())
        val empty2 = dataBlock(emptyList(), emptyList(), prefs())
        assertEquals(
            BackupContentFingerprint.ofData(empty),
            BackupContentFingerprint.ofData(empty2)
        )
        assertNotEquals(
            BackupContentFingerprint.ofData(empty),
            BackupContentFingerprint.ofData(complexData())
        )
    }

    // ── Version awareness: a migrated v1 backup matches the equivalent v3 content ─

    @Test
    fun `v1 backup migrated to current schema matches equivalent v3 content`() {
        // A schema-v1 history record had no device_id / updated_at / deleted_at; migration
        // backfills device_id="", updated_at=0, deleted_at=null (v1→v2) and then reshapes the
        // preferences (v2→v3). The comparison must treat a migrated v1 remote as equal to a v3
        // local holding the same logical content.
        val v1History = buildJsonObject {
            put("id", "h1"); put("amount", 250f); put("status", "IN_APP")
            put("timestamp", "2026-01-01T08:00:00")
        }
        val v1Goal = buildJsonObject {
            put("id", "g1"); put("effective_date", "2026-01-01"); put("goal_ml", 2000f)
        }
        val v1Envelope = envelope(
            data = buildJsonObject {
                put("history", buildJsonArray { add(v1History) })
                put("daily_goals", buildJsonArray { add(v1Goal) })
                put("preferences", prefs())
            },
            schemaVersion = 1,
        )

        val migrated = BackupMigrationManager.migrate(v1Envelope, fromSchema = 1)
        check(migrated is BackupResult.Success) { "migration failed: $migrated" }

        // The v2→v3 migration drops time_format_preference / water_animation_enabled and backfills
        // the active-hours fields with the canonical defaults — exactly what prefsV3() produces.
        val equivalentV3 = dataBlock(
            history = listOf(
                history(
                    id = "h1", amount = 250f, timestamp = "2026-01-01T08:00:00",
                    deviceId = "", updatedAt = 0L, deletedAt = null
                ),
            ),
            goals = listOf(
                goal(
                    id = "g1", effectiveDate = "2026-01-01", goalMl = 2000f,
                    deviceId = "", updatedAt = 0L, deletedAt = null
                ),
            ),
            preferences = prefsV3(),
        )

        assertEquals(
            BackupContentFingerprint.ofData(equivalentV3),
            BackupContentFingerprint.ofRemoteEnvelope(migrated.value),
        )
    }

    @Test
    fun `v2 backup migrated to current schema matches equivalent v3 content`() {
        val v2Envelope = envelope(
            data = dataBlock(emptyList(), emptyList(), prefs()),
            schemaVersion = 2,
        )

        val migrated = BackupMigrationManager.migrate(v2Envelope, fromSchema = 2)
        check(migrated is BackupResult.Success) { "migration failed: $migrated" }

        val equivalentV3 = dataBlock(emptyList(), emptyList(), prefsV3())

        assertEquals(
            BackupContentFingerprint.ofData(equivalentV3),
            BackupContentFingerprint.ofRemoteEnvelope(migrated.value),
        )
    }

    // ── Version locking: golden fingerprint for the CURRENT schema (v2) ──────────

    @Test
    fun `golden fingerprint for current schema v2 is stable`() {
        // Locks the canonicalization + data shape for schema_version = 2. If this value
        // changes, the comparison contract changed: remote backups written by older app
        // versions will stop matching. When the schema is bumped, ADD a new golden for the
        // new version rather than editing this one, so historical compatibility is documented.
        val data = dataBlock(
            history = listOf(
                history(
                    id = "hist-1", amount = 250f, status = "IN_APP",
                    timestamp = "2026-01-15T08:30:00", deviceId = "device-a",
                    updatedAt = 1_700_000_000_001L, deletedAt = null,
                ),
                history(
                    id = "hist-2", amount = 500f, status = "FROM_NOTIFICATION",
                    timestamp = "2026-01-15T12:00:00", deviceId = "device-b",
                    updatedAt = 1_700_000_000_002L, deletedAt = 1_700_000_500_000L,
                ),
            ),
            goals = listOf(
                goal(
                    id = "goal-1", effectiveDate = "2026-01-15", goalMl = 2500f,
                    deviceId = "device-a", updatedAt = 1_700_000_000_003L, deletedAt = null,
                ),
            ),
            preferences = prefs(
                username = "Elvis", amountPerDrink = 250f, unit = "ML",
                timeFormat = "SYSTEM", waterAnimation = true,
                bestGoalStreak = 7, bestDrinkStreak = 12,
            ),
        )

        assertEquals(GOLDEN_V2_FINGERPRINT, BackupContentFingerprint.ofData(data))
    }

    @Test
    fun `golden fingerprint for current schema v3 is stable`() {
        // Locks the canonicalization + data shape for schema_version = 3 (active hours + interval
        // + active days; no time format / water animation). Same contract as the v2 golden: if
        // this value changes, the comparison contract changed — bump deliberately.
        val data = dataBlock(
            history = listOf(
                history(
                    id = "hist-1", amount = 250f, status = "IN_APP",
                    timestamp = "2026-01-15T08:30:00", deviceId = "device-a",
                    updatedAt = 1_700_000_000_001L, deletedAt = null,
                ),
                history(
                    id = "hist-2", amount = 500f, status = "FROM_NOTIFICATION",
                    timestamp = "2026-01-15T12:00:00", deviceId = "device-b",
                    updatedAt = 1_700_000_000_002L, deletedAt = 1_700_000_500_000L,
                ),
            ),
            goals = listOf(
                goal(
                    id = "goal-1", effectiveDate = "2026-01-15", goalMl = 2500f,
                    deviceId = "device-a", updatedAt = 1_700_000_000_003L, deletedAt = null,
                ),
            ),
            preferences = prefsV3(
                username = "Elvis", amountPerDrink = 250f, unit = "ML",
                startHour = 10, endHour = 16, intervalMinute = 45,
                activeDays = listOf(1, 2, 3, 4, 5, 6, 7),
                bestGoalStreak = 7, bestDrinkStreak = 12,
            ),
        )

        assertEquals(GOLDEN_V3_FINGERPRINT, BackupContentFingerprint.ofData(data))
    }

    private companion object {
        // SHA-256 of the canonical form of the schema-v2 dataset above. See the test note.
        const val GOLDEN_V2_FINGERPRINT =
            "cdc9ec5db770a1b71e55e3225bf785bf55bc0b24c01fa9c5dae5d607cc10e194"

        // SHA-256 of the canonical form of the schema-v3 dataset above.
        const val GOLDEN_V3_FINGERPRINT =
            "e19d285131c5ef000b5e37004a3678c59957a1e7cdb08c8c5611182c7350fabc"
    }
}
