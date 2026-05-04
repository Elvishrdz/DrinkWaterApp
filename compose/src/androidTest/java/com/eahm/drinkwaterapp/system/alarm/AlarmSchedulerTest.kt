package com.eahm.drinkwaterapp.system.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.ALARM_PREFS_NAME
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.KEY_ACTIVE_ALARMS
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.keyTriggerAtMillis
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.keyTriggerTimeMillis
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [AlarmScheduler].
 *
 * On API 31+ the scheduler requires SCHEDULE_EXACT_ALARM permission. Tests that depend
 * on a successfully scheduled alarm use [Assume.assumeTrue] so they are skipped (not
 * failed) when the permission is not granted on the test device.
 */
@RunWith(AndroidJUnit4::class)
class AlarmSchedulerTest {

    companion object {
        private const val ALARM_ID = 42
        private const val INTERVAL_MS = 60_000L // 1 minute
    }

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
        AlarmScheduler.cancelAlarm(context, ALARM_ID)
    }

    @After
    fun tearDown() {
        AlarmScheduler.cancelAlarm(context, ALARM_ID)
        context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    // Returns false on API 31+ when the exact-alarm permission hasn't been granted
    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        } else {
            true
        }
    }

    // ── scheduleAlarm: storage ─────────────────────────────────────────────────────

    @Test
    fun scheduleAlarm_savesAlarmIdToActiveSet() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)

        val ids = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_ACTIVE_ALARMS, emptySet())
        assertTrue(ids?.contains(ALARM_ID.toString()) == true)
    }

    @Test
    fun scheduleAlarm_savesIntervalToStorage() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)

        val saved = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(keyTriggerTimeMillis(ALARM_ID), -1L)
        assertEquals(INTERVAL_MS, saved)
    }

    @Test
    fun scheduleAlarm_savesTriggerTimeWithinExpectedRange() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        val before = SystemClock.elapsedRealtime()
        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)
        val after = SystemClock.elapsedRealtime()

        val triggerAt = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(keyTriggerAtMillis(ALARM_ID), -1L)

        assertTrue("triggerAt should be >= before + interval", triggerAt >= before + INTERVAL_MS)
        assertTrue("triggerAt should be <= after + interval", triggerAt <= after + INTERVAL_MS)
    }

    // ── cancelAlarm: storage ───────────────────────────────────────────────────────

    @Test
    fun cancelAlarm_removesAlarmIdFromActiveSet() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)
        AlarmScheduler.cancelAlarm(context, ALARM_ID)

        val ids = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_ACTIVE_ALARMS, emptySet())
        assertFalse(ids?.contains(ALARM_ID.toString()) == true)
    }

    @Test
    fun cancelAlarm_removesIntervalFromStorage() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)
        AlarmScheduler.cancelAlarm(context, ALARM_ID)

        val saved = context.getSharedPreferences(ALARM_PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(keyTriggerTimeMillis(ALARM_ID), -1L)
        assertEquals(-1L, saved)
    }

    @Test
    fun cancelAlarm_onNeverScheduledAlarm_doesNotCrash() {
        // Should be a no-op, not a crash
        AlarmScheduler.cancelAlarm(context, 9999)
    }

    // ── isAlarmActive ──────────────────────────────────────────────────────────────

    @Test
    fun isAlarmActive_withoutScheduling_returnsFalse() {
        assertFalse(AlarmScheduler.isAlarmActive(context, ALARM_ID))
    }

    @Test
    fun isAlarmActive_afterScheduling_returnsTrue() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)

        assertTrue(AlarmScheduler.isAlarmActive(context, ALARM_ID))
    }

    @Test
    fun isAlarmActive_afterCancelling_returnsFalse() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)
        AlarmScheduler.cancelAlarm(context, ALARM_ID)

        assertFalse(AlarmScheduler.isAlarmActive(context, ALARM_ID))
    }

    // ── getTimeUntilNextTrigger ────────────────────────────────────────────────────

    @Test
    fun getTimeUntilNextTrigger_withoutScheduling_returnsNull() {
        assertNull(AlarmScheduler.getTimeUntilNextTrigger(context, ALARM_ID))
    }

    @Test
    fun getTimeUntilNextTrigger_afterScheduling_returnsValueInRange() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)

        val remaining = AlarmScheduler.getTimeUntilNextTrigger(context, ALARM_ID)
        assertNotNull(remaining)
        assertTrue("remaining should be >= 0", remaining!! >= 0L)
        assertTrue("remaining should be <= INTERVAL_MS", remaining <= INTERVAL_MS)
    }

    @Test
    fun getTimeUntilNextTrigger_afterCancelling_returnsNull() {
        Assume.assumeTrue("Requires SCHEDULE_EXACT_ALARM permission", canScheduleExactAlarms())

        AlarmScheduler.scheduleAlarm(context, ALARM_ID, INTERVAL_MS)
        AlarmScheduler.cancelAlarm(context, ALARM_ID)

        assertNull(AlarmScheduler.getTimeUntilNextTrigger(context, ALARM_ID))
    }
}
