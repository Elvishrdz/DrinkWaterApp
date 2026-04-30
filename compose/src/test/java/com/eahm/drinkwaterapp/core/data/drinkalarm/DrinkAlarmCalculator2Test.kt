package com.eahm.drinkwaterapp.core.data.drinkalarm

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
 *   startTime = 07:00, endTime = 22:00 → 15 h = 54 000 s
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
            goalAmountMl = 2_000,
            amountPerDrinkMl = 250f,
        )

        assertEquals(DayTime(7, 0), result.slots.first().time)
        assertEquals(DayTime(22, 0), result.slots.last().time)
    }

    @Test
    fun `scheduleByAmount - count is round(goal divided by amountPerDrink)`() {
        // 2 000 / 250 = 8.0 → 8 drinks
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            amountPerDrinkMl = 250f,
        )

        assertEquals(8, result.count)
        assertEquals(8, result.slots.size)
    }

    @Test
    fun `scheduleByAmount - sum of amounts equals goalAmountMl`() {
        val goalAmountMl = 2_000
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
            goalAmountMl = 2_000,
            amountPerDrinkMl = 300f,
        )

        assertEquals(7, result.count)
        assertEquals(300f, result.slots.first().amountMl)
        // 2 000 - 6 * 300 = 200 (exact in float)
        assertEquals(200f, result.slots.last().amountMl)
    }

    @Test
    fun `scheduleByAmount - sum equals goal when last slot adjusts`() {
        val goalAmountMl = 2_000
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
        // 54 000 s / 7 gaps = 7 714.28 s → DayTime.createWithSeconds(7714) = DayTime(2, 9)
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            amountPerDrinkMl = 250f,
        )

        assertEquals(DayTime(2, 9), result.actualInterval)
    }

    @Test
    fun `scheduleByAmount - uniform slot times for 8 drinks in 15h window`() {
        // 54 000 s, 7 gaps = 7 714.285 s each.
        // Computed: startSeconds=25200, exactInterval=7714.285714
        // i=1: round(7714.285)=7714  → abs=32914 → DayTime(9, 9)
        // i=2: round(15428.571)=15429 → abs=40629 → DayTime(11,17)
        // i=3: round(23142.857)=23143 → abs=48343 → DayTime(13,26)
        // i=4: round(30857.142)=30857 → abs=56057 → DayTime(15,34)
        // i=5: round(38571.428)=38571 → abs=63771 → DayTime(17,43)
        // i=6: round(46285.714)=46286 → abs=71486 → DayTime(19,51)
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            amountPerDrinkMl = 250f,
        )

        assertEquals(DayTime(7, 0), result.slots[0].time)
        assertEquals(DayTime(9, 9), result.slots[1].time)
        assertEquals(DayTime(11, 17), result.slots[2].time)
        assertEquals(DayTime(13, 26), result.slots[3].time)
        assertEquals(DayTime(15, 34), result.slots[4].time)
        assertEquals(DayTime(17, 43), result.slots[5].time)
        assertEquals(DayTime(19, 51), result.slots[6].time)
        assertEquals(DayTime(22, 0), result.slots[7].time)
    }

    @Test
    fun `scheduleByAmount - exact interval when window and count divide evenly`() {
        // 7:00-22:00 = 15h; 2 100ml / 300ml = 7 drinks = 6 gaps
        // 54 000 / 6 = 9 000 s = 2h 30m exactly
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_100,
            amountPerDrinkMl = 300f,
        )

        assertEquals(7, result.count)
        assertEquals(DayTime(2, 30), result.actualInterval)
        assertEquals(DayTime(7, 0), result.slots.first().time)
        assertEquals(DayTime(22, 0), result.slots.last().time)
    }

    @Test
    fun `scheduleByAmount - single drink when count rounds to 1`() {
        // 50 / 300 = 0.167 → round → 0 → coerced to 1
        val result = calculator.scheduleByAmount(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 50,
            amountPerDrinkMl = 300f,
        )

        assertEquals(1, result.count)
        assertEquals(DayTime(7, 0), result.slots.first().time)
        assertEquals(50f, result.slots.first().amountMl)
        assertEquals(DayTime(0, 0), result.actualInterval)
    }

    @Test
    fun `scheduleByAmount - two drinks placed at startTime and endTime`() {
        // 500 / 300 = 1.67 → round → 2 drinks
        val result = calculator.scheduleByAmount(
            startTime = DayTime(8, 0),
            endTime = DayTime(20, 0),
            goalAmountMl = 500,
            amountPerDrinkMl = 300f,
        )

        assertEquals(2, result.count)
        assertEquals(DayTime(8, 0), result.slots.first().time)
        assertEquals(DayTime(20, 0), result.slots.last().time)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mode B — scheduleByInterval
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scheduleByInterval - first slot is startTime and last slot is endTime`() {
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        assertEquals(DayTime(7, 0), result.slots.first().time)
        assertEquals(DayTime(22, 0), result.slots.last().time)
    }

    @Test
    fun `scheduleByInterval - adjusts interval to closest fit for non-exact window`() {
        // 15h = 54 000s, desired = 2h (7 200s) → gapsFloat = 7.5 → round → 8 gaps → 9 drinks
        // actualInterval = 54 000/8 = 6 750s → DayTime(1, 53)
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        assertEquals(9, result.count)
        assertEquals(DayTime(1, 53), result.actualInterval)
    }

    @Test
    fun `scheduleByInterval - exact fit when window is an exact multiple of desired`() {
        // 8:00-20:00 = 12h = 43 200s, desired = 2h → gapsFloat = 6.0 → 6 gaps → 7 drinks
        // actualInterval = 43 200/6 = 7 200s = 2h exactly
        val result = calculator.scheduleByInterval(
            startTime = DayTime(8, 0),
            endTime = DayTime(20, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        assertEquals(7, result.count)
        assertEquals(DayTime(2, 0), result.actualInterval)
        assertEquals(DayTime(8, 0), result.slots.first().time)
        assertEquals(DayTime(20, 0), result.slots.last().time)
    }

    @Test
    fun `scheduleByInterval - exact 1h interval when window divides by 1h`() {
        // 7:00-22:00 = 15h, desired = 1h → gapsFloat = 15.0 → 15 gaps → 16 drinks
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 60,
        )

        assertEquals(16, result.count)
        assertEquals(DayTime(1, 0), result.actualInterval)
    }

    @Test
    fun `scheduleByInterval - ties prefer more drinks`() {
        // gapsFloat = 7.5 → roundToInt ties toward +∞ → 8 gaps → 9 drinks (not 7 gaps → 8 drinks)
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        assertEquals(9, result.count)
    }

    @Test
    fun `scheduleByInterval - desired interval larger than window gives 2 drinks`() {
        // 8:00-10:00 = 2h = 7 200s, desired = 3h (10 800s) → gapsFloat = 0.67 → round → 1 gap → 2 drinks
        val result = calculator.scheduleByInterval(
            startTime = DayTime(8, 0),
            endTime = DayTime(10, 0),
            goalAmountMl = 1_000,
            desiredIntervalMinutes = 180,
        )

        assertEquals(2, result.count)
        assertEquals(DayTime(8, 0), result.slots.first().time)
        assertEquals(DayTime(10, 0), result.slots.last().time)
    }

    @Test
    fun `scheduleByInterval - sum of amounts equals goalAmountMl`() {
        val goalAmountMl = 2_000
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = goalAmountMl,
            desiredIntervalMinutes = 120,
        )

        val total = result.slots.sumOf { it.amountMl.toDouble() }
        assertTrue(abs(total - goalAmountMl) < 0.01, "Expected sum=$goalAmountMl, got=$total")
    }

    @Test
    fun `scheduleByInterval - uniform slot times for 9 drinks in 15h window`() {
        // 54 000s, 8 gaps = 6 750s each. startSeconds=25200.
        // i=1: abs=31950  → DayTime(8, 53)
        // i=2: abs=38700  → DayTime(10,45)
        // i=3: abs=45450  → DayTime(12,38)
        // i=4: abs=52200  → DayTime(14,30)
        // i=5: abs=58950  → DayTime(16,23)
        // i=6: abs=65700  → DayTime(18,15)
        // i=7: abs=72450  → DayTime(20, 8)
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        assertEquals(DayTime(7, 0), result.slots[0].time)
        assertEquals(DayTime(8, 53), result.slots[1].time)
        assertEquals(DayTime(10, 45), result.slots[2].time)
        assertEquals(DayTime(12, 38), result.slots[3].time)
        assertEquals(DayTime(14, 30), result.slots[4].time)
        assertEquals(DayTime(16, 23), result.slots[5].time)
        assertEquals(DayTime(18, 15), result.slots[6].time)
        assertEquals(DayTime(20, 8), result.slots[7].time)
        assertEquals(DayTime(22, 0), result.slots[8].time)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cross-mode: Mode B is strictly better than Mode A for interval accuracy
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scheduleByInterval is closer to desired interval than naive rounding`() {
        // 15h window, desired = 2h.
        // 8 gaps (9 drinks) → 1h52m deviation = 8 min.
        // 7 gaps (8 drinks) → 2h09m deviation = 9 min.
        // → Mode B picks 9 drinks (closer to 2h).
        val result = calculator.scheduleByInterval(
            startTime = DayTime(7, 0),
            endTime = DayTime(22, 0),
            goalAmountMl = 2_000,
            desiredIntervalMinutes = 120,
        )

        val actualIntervalMin = result.actualInterval.inWholeMinutes
        val desiredMin = 120

        // 8 gaps result: 54000/8=6750s=112.5min → 113min (1h53m), deviation=7min
        // 7 gaps result: 54000/7=7714s=128.6min → 129min (2h09m), deviation=9min
        // Mode B should give 9 drinks (8 gaps, 113min), which is closer.
        assertTrue(
            abs(actualIntervalMin - desiredMin) <= 8,
            "actualInterval=$actualIntervalMin min should be within 8 min of desired $desiredMin min"
        )
    }
}
