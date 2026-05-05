package com.eahm.drinkwaterapp.core.data.drinkalarm.data

import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [decideWhatToDoNow] covering boot-restore scenarios.
 *
 * After a device reboot, AlarmManager clears all pending alarms, so [hasActiveAlarm] is always
 * false. These tests verify that [decideWhatToDoNow] returns the correct action to re-register
 * or trigger alarms when called from the BOOT_COMPLETED BroadcastReceiver.
 *
 * Active window used throughout: 07:00 – 22:00 on 2026-05-05.
 * Simulated intervals (from SimulateNextIntervalCalculation): 10:00, 14:23, then endTime (22:00).
 */
class BootRestoreDeciderTest {

    private fun dt(hour: Int, minute: Int, second: Int = 0) =
        LocalDateTime(2026, 5, 5, hour, minute, second)

    private val startTime = dt(7, 0)
    private val endTime   = dt(22, 0)

    // ── Inside active window ──────────────────────────────────────────────────
    // Reboot happened while user was inside the active window.

    @Test
    fun `boot restore - inside active window - cached trigger in future - reschedules to next interval`() {
        // Alarm was set for 15:00. After reboot at 14:00, alarm is gone.
        // Decider should reschedule to the computed next interval (14:23).
        val result = decideWhatToDoNow(
            now = dt(14, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(15, 0),
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = dt(15, 0), correct = dt(14, 23)),
            result,
        )
    }

    @Test
    fun `boot restore - inside active window - cached trigger in past - remind now`() {
        // Alarm was set for 13:30. Boot took time, now it's 14:00 and the alarm was missed.
        // Decider should fire a reminder immediately.
        val result = decideWhatToDoNow(
            now = dt(14, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(13, 30),
        )
        assertEquals(WhatToDoNow.RemindNow, result)
    }

    @Test
    fun `boot restore - inside active window - no cached trigger - reschedules to next interval`() {
        // No previous alarm data at all; schedule fresh.
        val result = decideWhatToDoNow(
            now = dt(14, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = null,
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = null, correct = dt(14, 23)),
            result,
        )
    }

    @Test
    fun `boot restore - inside active window - cached trigger exactly at next interval - reschedules with no wrong`() {
        // Cached time matches the computed next interval exactly.
        // Still must reschedule (alarm is not active), wrong = null to signal "correct time".
        val result = decideWhatToDoNow(
            now = dt(14, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(14, 23),
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = null, correct = dt(14, 23)),
            result,
        )
    }

    // ── Before active window ──────────────────────────────────────────────────
    // Reboot happened before the active window opened (e.g. 05:00 or 06:00).

    @Test
    fun `boot restore - before active window - cached trigger at start of window - reschedules with no wrong`() {
        // Most common boot-restore case: alarm was correctly pointing at 07:00 (window start).
        // After reboot, alarm is gone; must re-register it. Before the fix this returned DoNothing.
        val result = decideWhatToDoNow(
            now = dt(6, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(7, 0),
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = null, correct = dt(7, 0)),
            result,
        )
    }

    @Test
    fun `boot restore - before active window - cached trigger at wrong future time - reschedules to start`() {
        // Alarm was pointing at 09:00 instead of the expected 07:00 (window start).
        val result = decideWhatToDoNow(
            now = dt(6, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(9, 0),
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = dt(9, 0), correct = dt(7, 0)),
            result,
        )
    }

    @Test
    fun `boot restore - before active window - no cached trigger - reschedules to start`() {
        val result = decideWhatToDoNow(
            now = dt(6, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = null,
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = null, correct = dt(7, 0)),
            result,
        )
    }

    // ── After active window ───────────────────────────────────────────────────
    // Reboot happened after the active window closed (e.g. 23:00).

    @Test
    fun `boot restore - after active window - no cached trigger - reschedules to next day start`() {
        // No alarm data; schedule for the next window start (07:00 tomorrow via scheduleAlarm h/m/s).
        val result = decideWhatToDoNow(
            now = dt(23, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = null,
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = null, correct = dt(7, 0)),
            result,
        )
    }

    @Test
    fun `boot restore - after active window - cached trigger at today 07 00 - considered past - reschedules`() {
        // Cached alarm points to 07:00 today but it's already 23:00; 07:00 < 23:00 so it's in the past.
        // Decider reschedules to the same 07:00 (tomorrow, handled by AlarmScheduler h/m/s overload).
        val result = decideWhatToDoNow(
            now = dt(23, 0),
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = dt(7, 0),
        )
        assertEquals(
            WhatToDoNow.RescheduleInterval(wrong = dt(7, 0), correct = dt(7, 0)),
            result,
        )
    }

    // ── Invalid / edge cases ──────────────────────────────────────────────────

    @Test
    fun `boot restore - invalid active hours - returns InvalidActiveHours`() {
        val sameTime = dt(8, 0)
        val result = decideWhatToDoNow(
            now = dt(14, 0),
            startTime = sameTime,
            endTime = sameTime,
            hasActiveAlarm = false,
            alarmAlarmTriggersAt = null,
        )
        assertEquals(WhatToDoNow.InvalidActiveHours, result)
    }
}
