package com.eahm.drinkwaterapp.backup

import com.eahm.drinkwaterapp.backup.data.BackupRecoveryResolver
import com.eahm.drinkwaterapp.backup.data.BackupSyncLock
import com.eahm.drinkwaterapp.backup.data.MergeEngine
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.usecase.PurgeTombstonesUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.HistoryRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import com.eahm.drinkwaterapp.backup.domain.model.TombstonePolicy
import com.eahm.drinkwaterapp.backup.fake.FakeBackupPreferences
import com.eahm.drinkwaterapp.backup.fake.InMemoryBackupDataSource
import com.eahm.drinkwaterapp.backup.fake.InMemoryCloudBackupProvider
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End-to-end backup scenarios driving the real [SyncBackupUseCase]/[RestoreBackupUseCase] +
 * [MergeEngine] against an in-memory data source and an in-memory cloud (round-tripping uploads).
 * Covers the user-listed cases: clean app, app-with-logs, merge-with-existing-backup,
 * deleted-records (tombstones), and restore-overwrites-local.
 */
class BackupSyncScenarioTest {

    // Based on real "now" because SyncBackupUseCase computes tombstone expiry from
    // System.currentTimeMillis(); a fixed past timestamp would count as already expired.
    private val now = System.currentTimeMillis()

    private fun history(
        id: String,
        device: String,
        amount: Float = 250f,
        updatedAt: Long = now,
        deletedAt: Long? = null,
    ) = HistoryRecord(
        id = id,
        amount = amount,
        status = "IN_APP",
        timestamp = now,
        deviceId = device,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    private fun backup(
        history: List<HistoryRecord> = emptyList(),
        goals: List<GoalRecord> = emptyList(),
        deviceId: String = "this-device",
    ) = BackupData(
        history = history,
        dailyGoals = goals,
        preferences = PreferencesRecord("ML", 250f, deviceId = deviceId),
    )

    private fun buildSync(dataSource: InMemoryBackupDataSource, prefs: FakeBackupPreferences) =
        SyncBackupUseCase(
            lock = BackupSyncLock(),
            dataSource = dataSource,
            prefs = prefs,
            mergeEngine = MergeEngine(),
            conflictResolver = SyncConflictResolver(),
            recoveryResolver = BackupRecoveryResolver(),
            purgeTombstonesUseCase = PurgeTombstonesUseCase(dataSource, prefs),
            tombstonePolicy = TombstonePolicy(),
        )

    private fun prefs(device: String = "this-device") = FakeBackupPreferences().apply {
        deviceId = device
        backupProvider = "google_drive"
    }

    private suspend fun envelopeOf(data: BackupData): String =
        InMemoryBackupDataSource(data).serialize(null)

    // 1 ── Clean app (no logs), no remote → first upload of empty data.
    @Test
    fun `scenario 1 - clean app with no logs uploads empty backup`() = runTest {
        val prefs = prefs()
        val dataSource = InMemoryBackupDataSource(backup())
        val provider = InMemoryCloudBackupProvider()

        val result = buildSync(dataSource, prefs)(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.Uploaded, result.value)
        assertEquals(1, provider.uploadCallCount)
        assertNotNull(prefs.backupLastSyncAt)
        assertNotNull(prefs.backupContentHash)
        // Remote now exists and has no history.
        val parsed = provider.download() as BackupResult.Success
        assertTrue(dataSource.parseRemoteData(parsed.value.rawData)!!.history.isEmpty())
    }

    // 2 ── App already has logs, no remote → uploads those logs.
    @Test
    fun `scenario 2 - app with existing logs uploads them`() = runTest {
        val prefs = prefs()
        val local =
            backup(history = listOf(history("a", "this-device"), history("b", "this-device")))
        val dataSource = InMemoryBackupDataSource(local)
        val provider = InMemoryCloudBackupProvider()

        val result = buildSync(dataSource, prefs)(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.Uploaded, result.value)
        val remote = (provider.download() as BackupResult.Success).value
        val uploaded = dataSource.parseRemoteData(remote.rawData)!!
        assertEquals(setOf("a", "b"), uploaded.history.map { it.id }.toSet())
    }

    // 3 ── Logs exist AND a remote backup exists → download, merge (union), re-upload.
    @Test
    fun `scenario 3 - merges local logs with existing remote backup`() = runTest {
        val prefs = prefs("device-A")
        val local = backup(history = listOf(history("a", "device-A")), deviceId = "device-A")
        val remote = backup(history = listOf(history("b", "device-B")), deviceId = "device-B")
        val dataSource = InMemoryBackupDataSource(local)
        val provider = InMemoryCloudBackupProvider(seedRemote = envelopeOf(remote))

        val result = buildSync(dataSource, prefs)(provider)

        assertTrue(result is BackupResult.Success)
        assertEquals(SyncResult.Uploaded, result.value)
        // Local DB now holds the union.
        assertEquals(setOf("a", "b"), dataSource.local.history.map { it.id }.toSet())
        // Remote was re-uploaded with the union.
        val merged = dataSource.parseRemoteData(provider.storedEnvelope()!!)!!
        assertEquals(setOf("a", "b"), merged.history.map { it.id }.toSet())
    }

    // 4 ── Local has a deleted record (tombstone) that remote still has live → deletion wins.
    @Test
    fun `scenario 4 - local tombstone is not resurrected by live remote record`() = runTest {
        val prefs = prefs("device-A")
        // Local: record X deleted recently (within retention).
        val local = backup(
            history = listOf(history("x", "device-A", updatedAt = now, deletedAt = now - 1_000)),
            deviceId = "device-A",
        )
        // Remote: same record X still live, with an older update.
        val remote = backup(
            history = listOf(history("x", "device-B", updatedAt = now - 10_000, deletedAt = null)),
            deviceId = "device-B",
        )
        val dataSource = InMemoryBackupDataSource(local)
        val provider = InMemoryCloudBackupProvider(seedRemote = envelopeOf(remote))

        val result = buildSync(dataSource, prefs)(provider)

        assertTrue(result is BackupResult.Success)
        val x = dataSource.local.history.single { it.id == "x" }
        assertNotNull(x.deletedAt) // stays deleted — not resurrected
    }

    // 5 ── Restore overwrites local data entirely with the remote backup.
    @Test
    fun `scenario 5 - restore overrides local data`() = runTest {
        val local =
            backup(history = listOf(history("local-only", "device-A")), deviceId = "device-A")
        val remote =
            backup(history = listOf(history("remote-only", "device-B")), deviceId = "device-B")
        val dataSource = InMemoryBackupDataSource(local)
        val provider = InMemoryCloudBackupProvider(seedRemote = envelopeOf(remote))

        val result = RestoreBackupUseCase(dataSource = dataSource)(provider = provider)

        assertTrue(result is BackupResult.Success)
        // Local replaced by remote — the local-only record is gone.
        assertEquals(listOf("remote-only"), dataSource.local.history.map { it.id })
        assertNull(dataSource.local.history.firstOrNull { it.id == "local-only" })
    }

    // 7 ── created_at is stamped once on the first upload and then preserved on every
    //      subsequent upload; only last_updated_at advances.
    @Test
    fun `scenario 7 - created_at preserved across uploads while last_updated_at advances`() =
        runTest {
            val prefs = prefs()
            val dataSource = InMemoryBackupDataSource(
                backup(history = listOf(history("a", "this-device"))),
            )
            val provider = InMemoryCloudBackupProvider()
            val sync = buildSync(dataSource, prefs)

            // First upload (no remote yet) stamps a fresh created_at.
            assertEquals(SyncResult.Uploaded, (sync(provider) as BackupResult.Success).value)
            val firstCreatedAt = createdAtOf(provider)
            val firstUpdatedAt = lastUpdatedOf(provider)

            // Local content changes → a second upload happens, but created_at must survive while
            // last_updated_at moves forward.
            dataSource.local = backup(
                history = listOf(history("a", "this-device"), history("b", "this-device")),
            )
            assertEquals(SyncResult.Uploaded, (sync(provider) as BackupResult.Success).value)

            assertEquals(firstCreatedAt, createdAtOf(provider))
            assertTrue(lastUpdatedOf(provider) >= firstUpdatedAt)
        }

    private fun createdAtOf(provider: InMemoryCloudBackupProvider): Long =
        provider.storedEnvelope()!!["metadata"]!!.jsonObject["created_at"]!!.jsonPrimitive.long

    private fun lastUpdatedOf(provider: InMemoryCloudBackupProvider): Long =
        provider.storedEnvelope()!!["metadata"]!!.jsonObject["last_updated_at"]!!.jsonPrimitive.long
}
