package com.eahm.drinkwaterapp.core.backup

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Layer B: exercises the REAL [BackupSerializer] against an in-memory Room database (via
 * Robolectric), verifying the production wire format and Room ↔ JSON mapping that the in-memory
 * Layer-A double can't cover: serialize → parse → writeBackupData round-trips with no data loss,
 * tombstones survive, and local/remote fingerprints match for identical content.
 */
@RunWith(RobolectricTestRunner::class)
// Plain Application so Robolectric doesn't boot the real BaseApplication (which starts Koin +
// WorkManager and crashes under the unit-test runtime).
@Config(sdk = [34], application = Application::class)
class BackupSerializerRoundTripTest {

    private lateinit var db: AppDatabase
    private lateinit var serializer: BackupSerializer

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val prefs = mockk<PrefsStorage>(relaxed = true)
        every { prefs.getUsername() } returns flowOf("Alice")
        every { prefs.getAmountPerDrink() } returns flowOf(250f)
        every { prefs.getUnitPreference() } returns flowOf(UnitPreference.ML)
        every { prefs.getStartTime() } returns flowOf(ActiveTime(10, 0))
        every { prefs.getEndTime() } returns flowOf(ActiveTime(16, 0))
        every { prefs.getInterval() } returns flowOf(ActiveTime(0, 45))
        every { prefs.getActiveDays() } returns flowOf(setOf(1, 2, 3, 4, 5, 6, 7))
        every { prefs.getBestGoalStreak() } returns flowOf(0)
        every { prefs.getBestDrinkStreak() } returns flowOf(0)

        val backupPrefs = mockk<BackupPreferences>(relaxed = true)
        coEvery { backupPrefs.getDeviceId() } returns "device-test"

        val appInfo = mockk<AppInfoService>()
        every { appInfo.versionName } returns "1.0.0"

        serializer = BackupSerializer(
            db,
            prefs,
            backupPrefs,
            appInfo,
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
        )
    }

    @After
    fun tearDown() = db.close()

    private fun parseEnvelope(serialized: String): JsonObject =
        Json.parseToJsonElement(serialized).jsonObject

    @Test
    fun `serialize then parse then write round-trips history and goals`() = runTest {
        val ts = LocalDateTime(2026, 6, 10, 8, 30, 0)
        db.getHistoryDao().insertAll(
            HistoryEntity(
                id = "h1",
                amount = 300f,
                status = Status.IN_APP,
                timestamp = ts,
                deviceId = "device-test",
                updatedAt = 111L,
                deletedAt = null,
            ),
        )
        db.getDailyGoalDao().insert(
            DailyGoalEntity(
                id = "g1",
                effectiveDate = LocalDate(2026, 6, 10),
                goalMl = 2000f,
                deviceId = "device-test",
                updatedAt = 222L,
            ),
        )

        val parsed = serializer.parseRemoteData(parseEnvelope(serializer.serialize(null)))
        assertNotNull(parsed)
        serializer.writeBackupData(parsed)

        val history = db.getHistoryDao().getAll().first()
        assertEquals(1, history.size)
        assertEquals("h1", history[0].id)
        assertEquals(300f, history[0].amount)
        assertEquals(ts, history[0].timestamp)
        assertEquals("device-test", history[0].deviceId)

        val goals = db.getDailyGoalDao().getAllGoals().first()
        assertEquals(1, goals.size)
        assertEquals("g1", goals[0].id)
        assertEquals(LocalDate(2026, 6, 10), goals[0].effectiveDate)
        assertEquals(2000f, goals[0].goalMl)
    }

    @Test
    fun `tombstone deletedAt survives the round-trip`() = runTest {
        db.getHistoryDao().insertAll(
            HistoryEntity(
                id = "deleted",
                amount = 100f,
                status = Status.IN_APP,
                timestamp = LocalDateTime(2026, 1, 1, 0, 0),
                deviceId = "device-test",
                updatedAt = 5L,
                deletedAt = 1_700_000_000_000L,
            ),
        )

        val parsed = serializer.parseRemoteData(parseEnvelope(serializer.serialize(null)))!!
        serializer.writeBackupData(parsed)

        val record = db.getHistoryDao().getAll().first().single()
        assertEquals(1_700_000_000_000L, record.deletedAt)
    }

    @Test
    fun `local and remote fingerprints match for identical content`() = runTest {
        db.getHistoryDao().insertAll(
            HistoryEntity(
                id = "h1",
                amount = 250f,
                status = Status.IN_APP,
                timestamp = LocalDateTime(2026, 6, 10, 8, 30),
                deviceId = "device-test",
                updatedAt = 1L,
            ),
        )

        val localFp = serializer.localContentFingerprint()
        val remoteFp = serializer.remoteContentFingerprint(parseEnvelope(serializer.serialize(null)))
        assertEquals(localFp, remoteFp)
    }
}
