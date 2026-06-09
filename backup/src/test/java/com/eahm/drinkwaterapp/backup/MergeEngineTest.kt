package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.HistoryRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MergeEngineTest {

    private val engine = MergeEngine()
    private val localDeviceId = "device-local"
    private val remoteDeviceId = "device-remote"
    private val now = System.currentTimeMillis()

    private fun emptyPrefs(updatedAt: Long = 0L) = PreferencesRecord(
        unitPreference = "ML", timeFormat = "SYSTEM", amountPerDrink = 250f, updatedAt = updatedAt
    )

    private fun historyRecord(
        id: String,
        amount: Float = 100f,
        deviceId: String = localDeviceId,
        updatedAt: Long = now,
        deletedAt: Long? = null,
    ) = HistoryRecord(
        id = id, amount = amount, status = "IN_APP", timestamp = now,
        deviceId = deviceId, updatedAt = updatedAt, deletedAt = deletedAt
    )

    private fun goalRecord(
        date: Int,
        goalMl: Float,
        deviceId: String = localDeviceId,
        updatedAt: Long = now,
        deletedAt: Long? = null,
    ) = GoalRecord(
        id = "goal-$date", effectiveDate = date, goalMl = goalMl,
        deviceId = deviceId, updatedAt = updatedAt, deletedAt = deletedAt
    )

    // ---- History tests ----

    @Test
    fun `history from two devices are unioned without duplicates`() {
        val local = BackupData(
            history = listOf(historyRecord("a"), historyRecord("b")),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = listOf(
                historyRecord("b", deviceId = remoteDeviceId),
                historyRecord("c", deviceId = remoteDeviceId)
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(3, result.mergedData.history.size)
        assertTrue(result.mergedData.history.any { it.id == "a" })
        assertTrue(result.mergedData.history.any { it.id == "b" })
        assertTrue(result.mergedData.history.any { it.id == "c" })
    }

    @Test
    fun `same record on both sides — newer updatedAt wins`() {
        val local = BackupData(
            history = listOf(historyRecord("a", amount = 100f, updatedAt = now - 10_000)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = listOf(
                historyRecord(
                    "a",
                    amount = 200f,
                    deviceId = remoteDeviceId,
                    updatedAt = now
                )
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.mergedData.history.size)
        assertEquals(200f, result.mergedData.history.first().amount)
        assertEquals(1, result.appliedRemoteRecords)
    }

    @Test
    fun `tombstone from remote applied when local copy is older`() {
        val local = BackupData(
            history = listOf(historyRecord("a", updatedAt = now - 10_000)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = listOf(
                historyRecord(
                    "a",
                    deviceId = remoteDeviceId,
                    updatedAt = now,
                    deletedAt = now
                )
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.mergedData.history.size)
        assertEquals(now, result.mergedData.history.first().deletedAt)
        assertEquals(1, result.tombstonesApplied)
    }

    @Test
    fun `tombstone not applied when local copy is newer`() {
        val local = BackupData(
            history = listOf(historyRecord("a", updatedAt = now)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = listOf(
                historyRecord(
                    "a",
                    deviceId = remoteDeviceId,
                    updatedAt = now - 10_000,
                    deletedAt = now - 10_000
                )
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.mergedData.history.size)
        assertEquals(null, result.mergedData.history.first().deletedAt)
        assertEquals(0, result.tombstonesApplied)
    }

    @Test
    fun `remote-only tombstone older than expiry is not re-added (Rule 3)`() {
        val local = BackupData(emptyList(), emptyList(), emptyPrefs())
        val remote = BackupData(
            history = listOf(
                historyRecord("a", deviceId = remoteDeviceId, updatedAt = now - 100_000, deletedAt = now - 100_000)
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        // expiry cutoff is AFTER the tombstone's deletedAt → expired → must be skipped
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = now - 50_000)
        assertTrue(result.mergedData.history.isEmpty())
        assertEquals(0, result.tombstonesApplied)
    }

    @Test
    fun `remote-only tombstone within retention is still applied`() {
        val local = BackupData(emptyList(), emptyList(), emptyPrefs())
        val remote = BackupData(
            history = listOf(
                historyRecord("a", deviceId = remoteDeviceId, updatedAt = now, deletedAt = now)
            ),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = now - 50_000)
        assertEquals(1, result.mergedData.history.size)
        assertEquals(now, result.mergedData.history.first().deletedAt)
        assertEquals(1, result.tombstonesApplied)
    }

    @Test
    fun `local tombstone within retention beats re-introduced remote live record (Rule 4)`() {
        val local = BackupData(
            history = listOf(historyRecord("a", updatedAt = now - 10_000, deletedAt = now - 10_000)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            // Remote live with NEWER updatedAt — a stale device re-introducing a deleted record.
            history = listOf(historyRecord("a", deviceId = remoteDeviceId, updatedAt = now, deletedAt = null)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = now - 60_000)
        assertEquals(1, result.mergedData.history.size)
        // Tombstone kept despite remote being "newer".
        assertEquals(now - 10_000, result.mergedData.history.first().deletedAt)
    }

    @Test
    fun `expired local tombstone does not force-win over newer remote live record`() {
        val local = BackupData(
            history = listOf(historyRecord("a", updatedAt = now - 100_000, deletedAt = now - 100_000)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = listOf(historyRecord("a", deviceId = remoteDeviceId, updatedAt = now, deletedAt = null)),
            dailyGoals = emptyList(), preferences = emptyPrefs()
        )
        // Tombstone deletedAt (now-100k) < expiry (now-50k) → not active → falls through to updatedAt.
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = now - 50_000)
        assertEquals(1, result.mergedData.history.size)
        assertEquals(null, result.mergedData.history.first().deletedAt)
    }

    // ---- Goal tests ----

    @Test
    fun `goal only on remote side is added`() {
        val local =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = emptyPrefs())
        val remote = BackupData(
            history = emptyList(),
            dailyGoals = listOf(goalRecord(date = 1000, goalMl = 2000f, deviceId = remoteDeviceId)),
            preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.mergedData.dailyGoals.size)
        assertEquals(2000f, result.mergedData.dailyGoals.first().goalMl)
    }

    @Test
    fun `goal conflict within 60s window is flagged`() {
        val local = BackupData(
            history = emptyList(),
            dailyGoals = listOf(goalRecord(date = 1000, goalMl = 2000f, updatedAt = now)),
            preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = emptyList(),
            dailyGoals = listOf(
                goalRecord(
                    date = 1000,
                    goalMl = 2500f,
                    deviceId = remoteDeviceId,
                    updatedAt = now - 30_000
                )
            ),
            preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.conflicts.size)
        assertIs<SyncConflict.GoalConflict>(result.conflicts.first())
        assertEquals(1000, (result.conflicts.first() as SyncConflict.GoalConflict).effectiveDate)
    }

    @Test
    fun `goal conflict outside 60s window is auto-resolved to newer`() {
        val local = BackupData(
            history = emptyList(),
            dailyGoals = listOf(goalRecord(date = 1000, goalMl = 2000f, updatedAt = now - 120_000)),
            preferences = emptyPrefs()
        )
        val remote = BackupData(
            history = emptyList(),
            dailyGoals = listOf(
                goalRecord(
                    date = 1000,
                    goalMl = 2500f,
                    deviceId = remoteDeviceId,
                    updatedAt = now
                )
            ),
            preferences = emptyPrefs()
        )
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertTrue(result.conflicts.isEmpty())
        assertEquals(1, result.autoResolvedConflicts)
        assertEquals(2500f, result.mergedData.dailyGoals.first().goalMl)
    }

    // ---- Prefs tests ----

    @Test
    fun `preferences conflict within 5 minutes is flagged`() {
        val localPrefs = PreferencesRecord("ML", "SYSTEM", 250f, updatedAt = now)
        val remotePrefs = PreferencesRecord("FL_OZ", "12H", 8f, updatedAt = now - 120_000)
        val local =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = localPrefs)
        val remote =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = remotePrefs)
        // local is newer, so we keep local — no conflict emitted (remote.updatedAt < local.updatedAt)
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        // local wins (updatedAt >= remote), conflict only emitted when remote wins
        assertEquals(0, result.conflicts.filterIsInstance<SyncConflict.PreferencesConflict>().size)
        assertEquals("ML", result.mergedData.preferences.unitPreference)
    }

    @Test
    fun `preferences remote newer within 5 minutes triggers conflict`() {
        val localPrefs = PreferencesRecord("ML", "SYSTEM", 250f, updatedAt = now - 120_000)
        val remotePrefs = PreferencesRecord("FL_OZ", "12H", 8f, updatedAt = now)
        val local =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = localPrefs)
        val remote =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = remotePrefs)
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertEquals(1, result.conflicts.filterIsInstance<SyncConflict.PreferencesConflict>().size)
        // Merged prefs is remote (remote is newer)
        assertEquals("FL_OZ", result.mergedData.preferences.unitPreference)
    }

    @Test
    fun `preferences remote newer outside 5 minutes is silent auto-resolve`() {
        val localPrefs = PreferencesRecord("ML", "SYSTEM", 250f, updatedAt = now - 600_000)
        val remotePrefs = PreferencesRecord("FL_OZ", "12H", 8f, updatedAt = now)
        val local =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = localPrefs)
        val remote =
            BackupData(history = emptyList(), dailyGoals = emptyList(), preferences = remotePrefs)
        val result = engine.merge(local, remote, localDeviceId, expiryThreshold = 0L)
        assertTrue(result.conflicts.isEmpty())
        assertEquals(1, result.autoResolvedConflicts)
        assertEquals("FL_OZ", result.mergedData.preferences.unitPreference)
    }
}
