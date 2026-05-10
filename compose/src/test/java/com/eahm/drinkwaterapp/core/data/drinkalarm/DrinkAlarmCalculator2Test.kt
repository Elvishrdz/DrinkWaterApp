package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.inWholeMinutes
import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [DrinkAlarmCalculator2].
 *
 * Key invariants verified:
 *  - slots.first().time == startTime, slots.last().time == endTime
 *  - slots.sumOf { amountMl } ≈ goalAmountMl
 *  - Uniform distribution: middle slots match expected rounded times
 *
 * Reference window used in most tests:
 *   startTime = 07:00, endTime = 22:00 → 15 h = 54 000 000 ms
 */
class DrinkAlarmCalculator2Test {

    private val calculator = DrinkAlarmCalculator2()

    // ─────────────────────────────────────────────────────────────────────────
    // Mode A — scheduleByAmount
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scheduleByAmount - first slot is startTime and last slot is endTime`() {
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000f,
            amountPerDrinkMl = 250f,
        )

        assertEquals(7, result.slots.first().time.hour)
        assertEquals(0, result.slots.first().time.minute)
        assertEquals(22, result.slots.last().time.hour)
        assertEquals(0, result.slots.last().time.minute)
    }

    @Test
    fun `scheduleByAmount - count is round(goal divided by amountPerDrink)`() {
        // 2 000 / 250 = 8.0 → 8 drinks
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000f,
            amountPerDrinkMl = 250f,
        )

        assertEquals(8, result.count)
        assertEquals(8, result.slots.size)
    }

    @Test
    fun `scheduleByAmount - sum of amounts equals goalAmountMl`() {
        val goalAmountMl = 2_000f
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = goalAmountMl,
            amountPerDrinkMl = 250f,
        )

        val total = result.slots.sumOf { it.amountMl.toDouble() }
        assertTrue(abs(total - goalAmountMl) < 0.01, "Expected sum=$goalAmountMl, got=$total")
    }

    @Test
    fun `scheduleByAmount - last slot adjusts when goal is not cleanly divisible`() {
        // 2 000 / 300 = 6.67 → 7 drinks; first 6 = 300 ml, last = 200 ml
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000f,
            amountPerDrinkMl = 300f,
        )

        assertEquals(7, result.count)
        assertEquals(300f, result.slots.first().amountMl)
        // 2 000 - 6 * 300 = 200 (exact in float)
        assertEquals(200f, result.slots.last().amountMl)
    }

    @Test
    fun `scheduleByAmount - sum equals goal when last slot adjusts`() {
        val goalAmountMl = 2_000f
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = goalAmountMl,
            amountPerDrinkMl = 300f,
        )

        val total = result.slots.sumOf { it.amountMl.toDouble() }
        assertTrue(abs(total - goalAmountMl) < 0.01, "Expected sum=$goalAmountMl, got=$total")
    }

    @Test
    fun `scheduleByAmount - actual interval for 8 drinks in 15h window`() {
        // 54 000 000 ms / 7 gaps = 7 714 285 ms → TimeResult(2, 8, 34, 285)
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000f,
            amountPerDrinkMl = 250f,
        )

        assertEquals(2, result.actualInterval.hour)
        assertEquals(8, result.actualInterval.minute)
    }

    @Test
    fun `scheduleByAmount - uniform slot times for 8 drinks in 15h window`() {
        // 54 000 000 ms, 7 gaps = 7 714 285 ms each. startMillis=25 200 000.
        // Slots: 07:00, 09:08, 11:17, 13:25, 15:34, 17:42, 19:51, 22:00
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000f,
            amountPerDrinkMl = 250f,
        )

        assertEquals(7, result.slots[0].time.hour); assertEquals(0, result.slots[0].time.minute)
        assertEquals(9, result.slots[1].time.hour); assertEquals(8, result.slots[1].time.minute)
        assertEquals(11, result.slots[2].time.hour); assertEquals(17, result.slots[2].time.minute)
        assertEquals(13, result.slots[3].time.hour); assertEquals(25, result.slots[3].time.minute)
        assertEquals(15, result.slots[4].time.hour); assertEquals(34, result.slots[4].time.minute)
        assertEquals(17, result.slots[5].time.hour); assertEquals(42, result.slots[5].time.minute)
        assertEquals(19, result.slots[6].time.hour); assertEquals(51, result.slots[6].time.minute)
        assertEquals(22, result.slots[7].time.hour); assertEquals(0, result.slots[7].time.minute)
    }

    @Test
    fun `scheduleByAmount - exact interval when window and count divide evenly`() {
        // 7:00-22:00 = 15h; 2 100ml / 300ml = 7 drinks = 6 gaps
        // 54 000 000 / 6 = 9 000 000 ms = 2h 30m exactly
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_100f,
            amountPerDrinkMl = 300f,
        )

        assertEquals(7, result.count)
        assertEquals(2, result.actualInterval.hour)
        assertEquals(30, result.actualInterval.minute)
        assertEquals(7, result.slots.first().time.hour); assertEquals(
            0,
            result.slots.first().time.minute
        )
        assertEquals(22, result.slots.last().time.hour); assertEquals(
            0,
            result.slots.last().time.minute
        )
    }

    @Test
    fun `scheduleByAmount - single drink when count rounds to 1`() {
        // 50 / 300 = 0.167 → round → 0 → coerced to 1
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 50f,
            amountPerDrinkMl = 300f,
        )

        assertEquals(1, result.count)
        assertEquals(7, result.slots.first().time.hour)
        assertEquals(0, result.slots.first().time.minute)
        assertEquals(50f, result.slots.first().amountMl)
        assertEquals(0, result.actualInterval.hour)
        assertEquals(0, result.actualInterval.minute)
    }

    @Test
    fun `scheduleByAmount - two drinks placed at startTime and endTime`() {
        // 500 / 300 = 1.67 → round → 2 drinks
        val result = calculator.scheduleByAmount(
            startTime = DayTime(8, 0),
            endTime = DayTime(20, 0),
            goalAmountMl = 500f,
            amountPerDrinkMl = 300f,
        )

        assertEquals(2, result.count)
        assertEquals(8, result.slots.first().time.hour); assertEquals(
            0,
            result.slots.first().time.minute
        )
        assertEquals(20, result.slots.last().time.hour); assertEquals(
            0,
            result.slots.last().time.minute
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mode B — scheduleByInterval
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scheduleByInterval - first slot is startTime and last slot is endTime`() {
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        assertEquals(7, result.slots.first().time.hour); assertEquals(
            0,
            result.slots.first().time.minute
        )
        assertEquals(22, result.slots.last().time.hour); assertEquals(
            0,
            result.slots.last().time.minute
        )
    }

    @Test
    fun `scheduleByInterval - adjusts interval to closest fit for non-exact window`() {
        // 15h = 54 000 000ms, desired = 2h (7 200 000ms) → gapsFloat = 7.5 → round → 8 gaps → 9 drinks
        // actualInterval = 54 000 000/8 = 6 750 000ms → TimeResult(1, 52, 30, 0)
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        assertEquals(9, result.count)
        assertEquals(1, result.actualInterval.hour)
        assertEquals(52, result.actualInterval.minute)
    }

    @Test
    fun `scheduleByInterval - exact fit when window is an exact multiple of desired`() {
        // 8:00-20:00 = 12h = 43 200 000ms, desired = 2h → gapsFloat = 6.0 → 6 gaps → 7 drinks
        // actualInterval = 43 200 000/6 = 7 200 000ms = 2h exactly
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(8, 0, 0, 0L),
            endTime = TimeResult(20, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        assertEquals(7, result.count)
        assertEquals(2, result.actualInterval.hour)
        assertEquals(0, result.actualInterval.minute)
        assertEquals(8, result.slots.first().time.hour); assertEquals(
            0,
            result.slots.first().time.minute
        )
        assertEquals(20, result.slots.last().time.hour); assertEquals(
            0,
            result.slots.last().time.minute
        )
    }

    @Test
    fun `scheduleByInterval - exact 1h interval when window divides by 1h`() {
        // 7:00-22:00 = 15h, desired = 1h → gapsFloat = 15.0 → 15 gaps → 16 drinks
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 60,
        )

        assertEquals(16, result.count)
        assertEquals(1, result.actualInterval.hour)
        assertEquals(0, result.actualInterval.minute)
    }

    @Test
    fun `scheduleByInterval - ties prefer more drinks`() {
        // gapsFloat = 7.5 → roundToInt ties toward +∞ → 8 gaps → 9 drinks (not 7 gaps → 8 drinks)
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        assertEquals(9, result.count)
    }

    @Test
    fun `scheduleByInterval - desired interval larger than window gives 2 drinks`() {
        // 8:00-10:00 = 2h = 7 200 000ms, desired = 3h (10 800 000ms) → gapsFloat = 0.67 → round → 1 gap → 2 drinks
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(8, 0, 0, 0L),
            endTime = TimeResult(10, 0, 0, 0L),
            goalAmountMl = 1_000f,
            desiredIntervalMinutes = 180,
        )

        assertEquals(2, result.count)
        assertEquals(8, result.slots.first().time.hour); assertEquals(
            0,
            result.slots.first().time.minute
        )
        assertEquals(10, result.slots.last().time.hour); assertEquals(
            0,
            result.slots.last().time.minute
        )
    }

    @Test
    fun `scheduleByInterval - sum of amounts equals goalAmountMl`() {
        val goalAmountMl = 2_000f
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = goalAmountMl,
            desiredIntervalMinutes = 120,
        )

        val total = result.slots.sumOf { it.amountMl.toDouble() }
        assertTrue(abs(total - goalAmountMl) < 0.01, "Expected sum=$goalAmountMl, got=$total")
    }

    @Test
    fun `scheduleByInterval - uniform slot times for 9 drinks in 15h window`() {
        // 54 000 000ms, 8 gaps = 6 750 000ms each. startMillis=25 200 000.
        // slot[i] = 25200000 + i*6750000 ms
        // i=1: 31950000 → 08:52:30
        // i=2: 38700000 → 10:45:00
        // i=3: 45450000 → 12:37:30
        // i=4: 52200000 → 14:30:00
        // i=5: 58950000 → 16:22:30
        // i=6: 65700000 → 18:15:00
        // i=7: 72450000 → 20:07:30
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        assertEquals(7, result.slots[0].time.hour); assertEquals(0, result.slots[0].time.minute)
        assertEquals(8, result.slots[1].time.hour); assertEquals(52, result.slots[1].time.minute)
        assertEquals(10, result.slots[2].time.hour); assertEquals(45, result.slots[2].time.minute)
        assertEquals(12, result.slots[3].time.hour); assertEquals(37, result.slots[3].time.minute)
        assertEquals(14, result.slots[4].time.hour); assertEquals(30, result.slots[4].time.minute)
        assertEquals(16, result.slots[5].time.hour); assertEquals(22, result.slots[5].time.minute)
        assertEquals(18, result.slots[6].time.hour); assertEquals(15, result.slots[6].time.minute)
        assertEquals(20, result.slots[7].time.hour); assertEquals(7, result.slots[7].time.minute)
        assertEquals(22, result.slots[8].time.hour); assertEquals(0, result.slots[8].time.minute)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cross-mode: Mode B is strictly better than Mode A for interval accuracy
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scheduleByInterval is closer to desired interval than naive rounding`() {
        // 15h window, desired = 2h.
        // 8 gaps (9 drinks) → 6 750 000ms = 1h 52m 30s deviation from 2h = 7m 30s
        // 7 gaps (8 drinks) → 7 714 285ms = 2h 8m 34s deviation from 2h = 8m 34s
        // → Mode B picks 9 drinks (closer to 2h).
        val result = calculator.scheduleByInterval(
            startTime = TimeResult(7, 0, 0, 0L),
            endTime = TimeResult(22, 0, 0, 0L),
            goalAmountMl = 2_000f,
            desiredIntervalMinutes = 120,
        )

        val actualIntervalMin = result.actualInterval.inWholeMinutes
        val desiredMin = 120

        // 8 gaps result: 54000000/8=6750000ms=112.5min, deviation=7.5min
        // 7 gaps result: 54000000/7=7714285ms=128.57min, deviation=8.57min
        // Mode B should give 9 drinks (8 gaps, 112min), which is closer.
        assertTrue(
            abs(actualIntervalMin - desiredMin) <= 8,
            "actualInterval=$actualIntervalMin min should be within 8 min of desired $desiredMin min"
        )
    }
}
