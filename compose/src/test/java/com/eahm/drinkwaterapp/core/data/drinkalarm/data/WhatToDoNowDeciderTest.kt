package com.eahm.drinkwaterapp.core.data.drinkalarm.data

import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [decideWhatToDoNow].
 *
 * Active window used in all tests: 07:00 – 22:00.
 * "Current time" is fixed at specific hours to place it clearly inside or outside that window.
 */
class WhatToDoNowDeciderTest {

    /** Constructs a LocalDateTime on a fixed date (2026-05-05). */
    private fun createLocalDateTime(hour: Int, minute: Int, second: Int = 0) =
        LocalDateTime(2026, 5, 5, hour, minute, second)

    private val startTime = createLocalDateTime(7, 0)
    private val endTime = createLocalDateTime(22, 0)

    // ── Inside active time ────────────────────────────────────────────────────
    // dateTime = 14:00 — squarely in the 07:00–22:00 window

    @Test
    fun `inside active time - no alarm - trigger in past - returns RemindNow`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = createLocalDateTime(13, 30),  // 30 min ago
        )
        assertEquals(WhatToDoNow.RemindNow, result)
    }

    @Test
    fun `inside active time - no alarm - trigger in future - returns ScheduleNextInterval`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = createLocalDateTime(15, 0),   // 1 h from now, within window
        )
        assertEquals(WhatToDoNow.ScheduleNextInterval, result)
    }

    @Test
    fun `inside active time - no alarm - no stored trigger - returns ScheduleNextInterval`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleNextInterval, result)
    }

    @Test
    fun `inside active time - alarm active - trigger in future within window - returns DoNothing`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(16, 30),  // future, before 22:00
        )
        assertEquals(WhatToDoNow.DoNothing, result)
    }

    @Test
    fun `inside active time - alarm active - trigger in past - returns RescheduleInterval`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(13, 30),  // already passed
        )
        assertEquals(WhatToDoNow.RescheduleInterval, result)
    }

    @Test
    fun `inside active time - alarm active - trigger beyond end time - returns RescheduleInterval`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(23, 0),   // after 22:00 end
        )
        assertEquals(WhatToDoNow.RescheduleInterval, result)
    }

    @Test
    fun `inside active time - alarm active - no stored trigger - returns RescheduleInterval`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.RescheduleInterval, result)
    }

    @Test
    fun `inside active time - alarm active - trigger exactly at end time - returns DoNothing`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(22, 0),   // exactly at endTime — boundary inclusive
        )
        assertEquals(WhatToDoNow.DoNothing, result)
    }

    // ── Outside active time — before window ───────────────────────────────────
    // dateTime = 06:00 — before the 07:00 start

    @Test
    fun `before active window - no alarm - returns ScheduleForActiveStart`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(6, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `before active window - alarm pointing at start time - returns DoNothing`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(6, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(7, 0),    // exact match: h=7, m=0
        )
        assertEquals(WhatToDoNow.DoNothing, result)
    }

    @Test
    fun `before active window - alarm pointing at wrong time - returns ScheduleForActiveStart`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(6, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(9, 0),    // active window, but not the start
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `before active window - alarm active but no stored trigger - returns ScheduleForActiveStart`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(6, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    // dateTime = 23:00 — after the 22:00 end
    @Test
    fun `after active window - no alarm - returns ScheduleForActiveStart`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(23, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `after active window - alarm pointing at tomorrows start hour - returns DoNothing`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(23, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(
                7,
                0
            ),    // same h:m as startTime — tomorrow's alarm
        )
        assertEquals(WhatToDoNow.DoNothing, result)
    }

    @Test
    fun `after active window - alarm pointing at wrong time - returns ScheduleForActiveStart`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(23, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(10, 0),   // h:m doesn't match startTime
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `equal start and end time - no alarm - returns ScheduleForActiveStart`() {
        val sameTime = createLocalDateTime(8, 0)
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = sameTime,
            endTime = sameTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `equal start and end time - alarm at that same time - returns DoNothing`() {
        val sameTime = createLocalDateTime(8, 0)
        val result = decideWhatToDoNow(
            now = createLocalDateTime(14, 0),
            startTime = sameTime,
            endTime = sameTime,
            alarmActive = true,
            storedTrigger = createLocalDateTime(8, 0),
        )
        // isInActiveTime = false (startTime == endTime guard), falls to outside branch.
        // Alarm h:m matches startTime h:m → DoNothing.
        assertEquals(WhatToDoNow.DoNothing, result)
    }

    @Test
    fun `dateTime exactly at start time - counts as inside window`() {
        // 07:00 in 07:00..22:00 → true
        val result = decideWhatToDoNow(
            now = createLocalDateTime(7, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleNextInterval, result)
    }

    @Test
    fun `dateTime exactly at end time - counts as inside window`() {
        // 22:00 in 07:00..22:00 → true (ClosedRange includes end)
        val result = decideWhatToDoNow(
            now = createLocalDateTime(22, 0),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleNextInterval, result)
    }

    @Test
    fun `dateTime one second before start - counts as outside window`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(6, 59, 59),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }

    @Test
    fun `dateTime one second after end - counts as outside window`() {
        val result = decideWhatToDoNow(
            now = createLocalDateTime(22, 0, 1),
            startTime = startTime,
            endTime = endTime,
            alarmActive = false,
            storedTrigger = null,
        )
        assertEquals(WhatToDoNow.ScheduleForActiveStart, result)
    }
}
