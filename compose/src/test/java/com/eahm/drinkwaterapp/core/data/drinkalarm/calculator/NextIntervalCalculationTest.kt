package com.eahm.drinkwaterapp.core.data.drinkalarm.calculator

import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.simulator.createSimulatedDateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

/**
 * Tests for [calculateNextAlarmSlot].
 *
 * Schedule used by all tests: start=09:00, end=18:00, goalMl=2000, interval=120 min.
 * Window = 540 min → 5 gaps → 6 drinks at 108-minute intervals:
 *   09:00, 10:48, 12:36, 14:24, 16:12, 18:00
 */
@OptIn(ExperimentalTime::class)
class NextIntervalCalculationTest {

    private val start = DayTime(9, 0)
    private val end = DayTime(18, 0)
    private val goalMl = 2_000f
    private val intervalMinutes = 120

    @Test
    fun `before window returns startTime today`() {
        val now = createSimulatedDateTime(7, 0)

        val result = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )

        val expected = createSimulatedDateTime(9, 0)
        assertEquals(expected.hour, result.hour)
        assertEquals(expected.minute, result.minute)
    }

    @Test
    fun `at start slot boundary picks second slot`() {
        // now == 09:00:00 → strict > skips the 09:00 slot → next is 10:48
        val now = createSimulatedDateTime(9, 0)

        val result = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )

        assertEquals(10, result.hour)
        assertEquals(48, result.minute)
    }

    @Test
    fun `mid window returns next slot after now`() {
        // now = 12:00 → slots before/at 12:00 are 09:00, 10:48; first after is 12:36
        val now = createSimulatedDateTime(12, 0)

        val result = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )

        assertEquals(12, result.hour)
        assertEquals(36, result.minute)
    }

    @Test
    fun `after window falls back to startTime today`() {
        // now = 19:00, past all slots → fallback to startTime (scheduler fires tomorrow)
        val now = createSimulatedDateTime(19, 0)

        val result = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )

        assertEquals(start.hour, result.hour)
        assertEquals(start.minute, result.minute)
    }

    @Test
    fun `same inputs produce identical results`() {
        val now = createSimulatedDateTime(11, 0)

        val result1 = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )
        val result2 = calculateNextAlarmSlot(
            now = now,
            startTime = start,
            endTime = end,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )

        assertEquals(result1, result2)
    }
}