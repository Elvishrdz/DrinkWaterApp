package com.eahm.drinkwaterapp.core.data.drinkalarm

/**
 * Determines when the next drink alarm should fire given the current time.
 *
 * Pure class — no Android dependencies. Fully testable with JVM unit tests.
 *
 * Cases:
 * 1. [NextAlarmResult.GoalReached]  — remainingMl ≤ 0; cancel and do not reschedule.
 * 2. [NextAlarmResult.BeforeWindow] — now is before startTime; delay until window opens.
 * 3. [NextAlarmResult.AfterWindow]  — now is at or after endTime; delay until tomorrow's startTime.
 * 4. [NextAlarmResult.Schedule]     — within window, multiple drinks left; next drink = slots[1].
 * 5. [NextAlarmResult.LastDrink]    — within window, only one drink slot (count == 1); done for
 *                                    today, delay until tomorrow's startTime.
 */
class NextAlarmCalculator(
    private val calculator: DrinkAlarmCalculator2 = DrinkAlarmCalculator2(),
) {

    fun calculate(
        now: DayTime,
        startTime: DayTime,
        endTime: DayTime,
        remainingMl: Float,
        amountPerDrinkMl: Float,
    ): NextAlarmResult {
        // Case 1: goal already reached
        if (remainingMl <= 0f) return NextAlarmResult.GoalReached

        val nowMinutes = now.inWholeMinutes
        val startMinutes = startTime.inWholeMinutes
        val endMinutes = endTime.inWholeMinutes

        val withinWindow = isWithinWindow(nowMinutes, startMinutes, endMinutes)

        // Case 2: before the active window
        if (!withinWindow && isBeforeWindow(nowMinutes, startMinutes, endMinutes)) {
            return NextAlarmResult.BeforeWindow(
                delayMillis = minutesDiff(now, startTime) * 60_000L,
            )
        }

        // Case 3: at or after the window end (includes zero-length window where start == end)
        if (!withinWindow) {
            return NextAlarmResult.AfterWindow(
                delayMillis = tomorrowDelayMillis(now, startTime),
            )
        }

        // Cases 4 & 5: within window
        val schedule = calculator.scheduleByAmount(
            startTime = now,
            endTime = endTime,
            goalAmountMl = remainingMl,
            amountPerDrinkMl = amountPerDrinkMl,
        )

        return if (schedule.count == 1) {
            // Case 5: last drink is happening now; reschedule for tomorrow
            NextAlarmResult.LastDrink(
                delayMillis = tomorrowDelayMillis(now, startTime),
            )
        } else {
            // Case 4: next drink is the second slot (slots[0] == now)
            val nextSlot = schedule.slots[1]
            NextAlarmResult.Schedule(
                delayMillis = minutesDiff(
                    now,
                    DayTime(nextSlot.time.hour, nextSlot.time.minute)
                ) * 60_000L,
                nextSlot = nextSlot,
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns true if [nowMinutes] falls within [startMinutes, endMinutes).
     *
     * Handles both normal windows (end > start) and overnight windows (end < start).
     * A zero-length window (start == end) is never "within".
     */
    private fun isWithinWindow(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        if (startMinutes == endMinutes) return false
        return if (endMinutes > startMinutes) {
            // Normal window: e.g. 07:00–22:00
            nowMinutes >= startMinutes && nowMinutes < endMinutes
        } else {
            // Overnight window: e.g. 22:00–06:00
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }

    /**
     * Returns true if [nowMinutes] comes before the window opens (normal windows only).
     * For overnight windows, every moment outside the window is handled as AfterWindow.
     */
    private fun isBeforeWindow(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
        if (startMinutes == endMinutes) return false
        return if (endMinutes > startMinutes) {
            nowMinutes < startMinutes
        } else {
            // Overnight window: times outside the window are between endMinutes and startMinutes.
            // We treat them all as AfterWindow (next occurrence of startTime).
            false
        }
    }

    /** Minutes from [from] to [to], handling overnight wrap via [DayTime.calculateTimeDifference]. */
    private fun minutesDiff(from: DayTime, to: DayTime): Long =
        DayTime.calculateTimeDifference(from, to).inWholeMinutes.toLong()

    /**
     * Milliseconds until tomorrow's [startTime].
     *
     * [DayTime.calculateTimeDifference] gives the correct overnight diff when [now] > [startTime].
     * When [now] == [startTime] the difference is 0 — in that case we want exactly 24 h.
     */
    private fun tomorrowDelayMillis(now: DayTime, startTime: DayTime): Long {
        val diffMinutes = minutesDiff(now, startTime)
        val adjustedMinutes = if (diffMinutes == 0L) 24 * 60L else diffMinutes
        return adjustedMinutes * 60_000L
    }
}
