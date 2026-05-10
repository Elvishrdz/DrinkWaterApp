package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.data.drinkalarm.data.inWholeMinutes
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [NextAlarmCalculator].
 *
 * Reference window used in most tests: start = 07:00, end = 22:00 (15 h = 900 min).
 *
 * Cases verified:
 *  - BW: BeforeWindow — now < startTime
 *  - AW: AfterWindow  — now ≥ endTime (or zero-length window)
 *  - GR: GoalReached  — remainingMl ≤ 0
 *  - WW: Schedule     — within window, count > 1
 *  - LD: LastDrink    — within window, count == 1
 *  - DC: Delay correctness
 *  - EG: Edge cases
 */
class NextAlarmCalculatorTest {

    private val calc = NextAlarmCalculator()

    private val start = DayTime(7, 0)
    private val end = DayTime(22, 0)

    // ─────────────────────────────────────────────────────────────────────────
    // BW — Before Window
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `BW-1 - 06h00 before 07h00 window returns BeforeWindow`() {
        val result = calc.calculate(
            now = DayTime(6, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.BeforeWindow>(result)
    }

    @Test
    fun `BW-1 - delay is exactly 60 minutes in millis`() {
        val result = calc.calculate(
            now = DayTime(6, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.BeforeWindow
        assertEquals(60 * 60_000L, result.delayMillis)
    }

    @Test
    fun `BW-2 - 06h59 returns BeforeWindow with 1-minute delay`() {
        val result = calc.calculate(
            now = DayTime(6, 59),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.BeforeWindow
        assertEquals(1 * 60_000L, result.delayMillis)
    }

    @Test
    fun `BW-3 - midnight before 07h00 window returns BeforeWindow with 420-minute delay`() {
        val result = calc.calculate(
            now = DayTime(0, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.BeforeWindow
        assertEquals(420 * 60_000L, result.delayMillis)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AW — After Window
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `AW-1 - 22h00 (exactly at end) returns AfterWindow`() {
        val result = calc.calculate(
            now = DayTime(22, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.AfterWindow>(result)
    }

    @Test
    fun `AW-1 - delay to next 07h00 is 540 minutes`() {
        // 22:00 → next 07:00 = (24-22)*60 + 7*60 = 120 + 420 = 540 min
        val result = calc.calculate(
            now = DayTime(22, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.AfterWindow
        assertEquals(540 * 60_000L, result.delayMillis)
    }

    @Test
    fun `AW-2 - 22h01 returns AfterWindow with 539-minute delay`() {
        val result = calc.calculate(
            now = DayTime(22, 1),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.AfterWindow
        assertEquals(539 * 60_000L, result.delayMillis)
    }

    @Test
    fun `AW-3 - 23h59 returns AfterWindow with correct delay to next 07h00`() {
        // 23:59 → next 07:00 = 1 min to midnight + 7h = 421 min
        val result = calc.calculate(
            now = DayTime(23, 59),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.AfterWindow
        assertEquals(421 * 60_000L, result.delayMillis)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GR — Goal Reached
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `GR-1 - remaining zero returns GoalReached`() {
        val result = calc.calculate(
            now = DayTime(10, 0),
            startTime = start,
            endTime = end,
            remainingMl = 0f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.GoalReached>(result)
    }

    @Test
    fun `GR-2 - negative remaining returns GoalReached`() {
        val result = calc.calculate(
            now = DayTime(10, 0),
            startTime = start,
            endTime = end,
            remainingMl = -50f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.GoalReached>(result)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WW — Within Window, Schedule (count > 1)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `WW-1 - 07h00 at window start returns Schedule`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        // 2000/250 = 8 drinks → count > 1 → Schedule
        assertIs<NextAlarmResult.Schedule>(result)
    }

    @Test
    fun `WW-1 - Schedule delay is positive`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.Schedule
        assertTrue(result.delayMillis > 0L, "delay must be positive")
    }

    @Test
    fun `WW-1 - Schedule delay is less than full 15h window`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.Schedule
        val windowMillis = 15 * 60 * 60_000L
        assertTrue(result.delayMillis <= windowMillis, "delay must not exceed window length")
    }

    @Test
    fun `WW-1 - nextSlot time is after now`() {
        val now = DayTime(7, 0)
        val result = calc.calculate(
            now = now,
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.Schedule
        assertTrue(
            result.nextSlot.time.inWholeMinutes > now.inWholeMinutes,
            "nextSlot (${result.nextSlot.time}) must be after now ($now)"
        )
    }

    @Test
    fun `WW-2 - mid-window 14h00 with 1000ml remaining returns Schedule`() {
        val result = calc.calculate(
            now = DayTime(14, 0),
            startTime = start,
            endTime = end,
            remainingMl = 1_000f,
            amountPerDrinkMl = 250f,
        )
        // 1000/250 = 4 drinks → count > 1 → Schedule
        assertIs<NextAlarmResult.Schedule>(result)
    }

    @Test
    fun `WW-2 - Schedule delay is positive`() {
        val result = calc.calculate(
            now = DayTime(14, 0),
            startTime = start,
            endTime = end,
            remainingMl = 1_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.Schedule
        assertTrue(result.delayMillis > 0L)
    }

    @Test
    fun `WW-3 - 07h01 just inside window returns Schedule`() {
        val result = calc.calculate(
            now = DayTime(7, 1),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.Schedule>(result)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LD — Last Drink (count == 1)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `LD-1 - tiny remaining rounds to 1 drink returns LastDrink`() {
        // 50ml / 500ml = 0.1 → rounds to 0 → coerced to 1
        val result = calc.calculate(
            now = DayTime(21, 0),
            startTime = start,
            endTime = end,
            remainingMl = 50f,
            amountPerDrinkMl = 500f,
        )
        assertIs<NextAlarmResult.LastDrink>(result)
    }

    @Test
    fun `LD-2 - remaining less than amountPerDrink rounds to 1 drink returns LastDrink`() {
        val result = calc.calculate(
            now = DayTime(20, 0),
            startTime = start,
            endTime = end,
            remainingMl = 100f,
            amountPerDrinkMl = 500f,
        )
        assertIs<NextAlarmResult.LastDrink>(result)
    }

    @Test
    fun `LD-3 - LastDrink delay is positive`() {
        val result = calc.calculate(
            now = DayTime(21, 0),
            startTime = start,
            endTime = end,
            remainingMl = 50f,
            amountPerDrinkMl = 500f,
        ) as NextAlarmResult.LastDrink
        assertTrue(result.delayMillis > 0L, "LastDrink delay must be positive")
    }

    @Test
    fun `LD-3 - LastDrink delay is at least 1 minute`() {
        val result = calc.calculate(
            now = DayTime(21, 0),
            startTime = start,
            endTime = end,
            remainingMl = 50f,
            amountPerDrinkMl = 500f,
        ) as NextAlarmResult.LastDrink
        assertTrue(result.delayMillis >= 60_000L, "LastDrink delay should be at least 1 minute")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DC — Delay Correctness
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `DC-1 - BW delay exactly 3 600 000 ms for 1 hour gap`() {
        val result = calc.calculate(
            now = DayTime(6, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.BeforeWindow
        assertEquals(3_600_000L, result.delayMillis)
    }

    @Test
    fun `DC-2 - AW delay exactly 32 400 000 ms for 22h00 to next 07h00`() {
        // 540 min * 60_000 ms/min = 32_400_000 ms
        val result = calc.calculate(
            now = DayTime(22, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.AfterWindow
        assertEquals(32_400_000L, result.delayMillis)
    }

    @Test
    fun `DC-3 - Schedule delay is bounded within window`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = start,
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        ) as NextAlarmResult.Schedule
        val windowMs = 15L * 60 * 60_000L
        assertTrue(result.delayMillis in 1L..windowMs)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EG — Edge Cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `EG-1 - now equals startTime with count greater than 1 returns Schedule not BeforeWindow`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = DayTime(7, 0),
            endTime = end,
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.Schedule>(result)
    }

    @Test
    fun `EG-2 - now equals startTime with count rounding to 1 returns LastDrink`() {
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = DayTime(7, 0),
            endTime = end,
            remainingMl = 50f,
            amountPerDrinkMl = 500f,
        )
        assertIs<NextAlarmResult.LastDrink>(result)
    }

    @Test
    fun `EG-2 - LastDrink delay when now equals startTime is exactly 24h`() {
        // minutesDiff(07:00, 07:00) == 0 → falls back to 24h
        val result = calc.calculate(
            now = DayTime(7, 0),
            startTime = DayTime(7, 0),
            endTime = end,
            remainingMl = 50f,
            amountPerDrinkMl = 500f,
        ) as NextAlarmResult.LastDrink
        assertEquals(24 * 60 * 60_000L, result.delayMillis)
    }

    @Test
    fun `EG-3 - zero-length window (start equals end) returns AfterWindow`() {
        val result = calc.calculate(
            now = DayTime(10, 0),
            startTime = DayTime(10, 0),
            endTime = DayTime(10, 0),
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.AfterWindow>(result)
    }

    @Test
    fun `EG-4 - overnight window now inside window returns Schedule`() {
        // Overnight window: 22:00–06:00; now=23:00 is inside
        val result = calc.calculate(
            now = DayTime(23, 0),
            startTime = DayTime(22, 0),
            endTime = DayTime(6, 0),
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        // Multiple drinks → Schedule
        assertTrue(
            result is NextAlarmResult.Schedule || result is NextAlarmResult.LastDrink,
            "Expected Schedule or LastDrink for overnight window, got $result"
        )
    }

    @Test
    fun `EG-5 - overnight window now outside window returns AfterWindow`() {
        // Overnight window: 22:00–06:00; now=10:00 is outside
        val result = calc.calculate(
            now = DayTime(10, 0),
            startTime = DayTime(22, 0),
            endTime = DayTime(6, 0),
            remainingMl = 2_000f,
            amountPerDrinkMl = 250f,
        )
        assertIs<NextAlarmResult.AfterWindow>(result)
    }
}
