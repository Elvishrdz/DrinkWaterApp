package com.eahm.drinkwaterapp.core.data.drinkalarm

import kotlin.math.roundToInt

data class DrinkSlot(
    val time: DayTime,
    val amountMl: Float,
)

data class DrinkSchedule(
    val slots: List<DrinkSlot>,
    /** Actual interval between consecutive drinks (computed from exact distribution). */
    val actualInterval: DayTime,
    val count: Int,
)

/**
 * Improved drink schedule calculator that distributes drink times uniformly
 * between [startTime] and [endTime].
 *
 * Contract:
 * - First slot is always at [startTime], last slot is always at [endTime].
 * - N drinks produce N-1 equal gaps — no slot ever exceeds [endTime].
 * - Middle slots are placed at `startTime + i * (totalTime / (N-1))` rounded to the nearest second.
 * - The last slot's [DrinkSlot.amountMl] is adjusted so the total equals [goalAmountMl] exactly.
 *
 * Two scheduling modes:
 * - [scheduleByAmount]: infers drink count from `goalAmountMl / amountPerDrinkMl`.
 * - [scheduleByInterval]: finds the integer gap count whose actual interval is closest
 *   to [desiredIntervalMinutes] (ties favour more drinks).
 */
class DrinkAlarmCalculator2 {

    /**
     * Schedule drinks by amount per drink.
     *
     * Count = round(goalAmountMl / amountPerDrinkMl), minimum 1.
     *
     * @param startTime  First drink time (inclusive).
     * @param endTime    Last drink time (inclusive). Must be after [startTime].
     * @param goalAmountMl  Total hydration goal in ml.
     * @param amountPerDrinkMl  Preferred ml per session (last slot adjusts to hit exact goal).
     */
    fun scheduleByAmount(
        startTime: DayTime,
        endTime: DayTime,
        goalAmountMl: Int,
        amountPerDrinkMl: Float,
    ): DrinkSchedule {
        require(amountPerDrinkMl > 0f) { "amountPerDrinkMl must be positive" }
        require(goalAmountMl > 0) { "goalAmountMl must be positive" }

        val count = (goalAmountMl / amountPerDrinkMl).roundToInt().coerceAtLeast(1)
        return buildSchedule(
            startTime = startTime,
            endTime = endTime,
            count = count,
            goalAmountMl = goalAmountMl,
            baseAmountPerDrinkMl = amountPerDrinkMl,
        )
    }

    /**
     * Schedule drinks by desired time interval.
     *
     * Finds the integer gap count whose resulting interval is closest to [desiredIntervalMinutes].
     * When two options are equidistant, the option with more gaps (more drinks) is preferred,
     * keeping each drink smaller and the schedule denser.
     *
     * Example: 15 h window, desired 2 h → 7.5 gaps → rounded to 8 gaps → 9 drinks at 1 h 52 m each.
     *
     * @param startTime  First drink time (inclusive).
     * @param endTime    Last drink time (inclusive). Must be after [startTime].
     * @param goalAmountMl  Total hydration goal in ml.
     * @param desiredIntervalMinutes  Preferred gap between drinks in minutes.
     *   The actual interval will be adjusted to fit the window perfectly.
     */
    fun scheduleByInterval(
        startTime: DayTime,
        endTime: DayTime,
        goalAmountMl: Int,
        desiredIntervalMinutes: Int,
    ): DrinkSchedule {
        require(desiredIntervalMinutes > 0) { "desiredIntervalMinutes must be positive" }
        require(goalAmountMl > 0) { "goalAmountMl must be positive" }

        val totalSeconds = windowSeconds(startTime, endTime)
        val desiredIntervalSeconds = desiredIntervalMinutes * 60

        // gaps = N-1 for N drinks; find the integer gap count closest to the desired interval.
        // roundToInt() rounds ties toward +∞ → prefers more gaps = more drinks.
        val gapsFloat = totalSeconds.toFloat() / desiredIntervalSeconds.toFloat()
        val gaps = gapsFloat.roundToInt().coerceAtLeast(1)
        val count = gaps + 1

        val baseAmountPerDrink = goalAmountMl.toFloat() / count.toFloat()
        return buildSchedule(
            startTime = startTime,
            endTime = endTime,
            count = count,
            goalAmountMl = goalAmountMl,
            baseAmountPerDrinkMl = baseAmountPerDrink,
        )
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun buildSchedule(
        startTime: DayTime,
        endTime: DayTime,
        count: Int,
        goalAmountMl: Int,
        baseAmountPerDrinkMl: Float,
    ): DrinkSchedule {
        // Edge case: single drink — no meaningful interval, no endTime pin.
        if (count == 1) {
            return DrinkSchedule(
                slots = listOf(DrinkSlot(time = startTime, amountMl = goalAmountMl.toFloat())),
                actualInterval = DayTime(0, 0),
                count = 1,
            )
        }

        val totalSeconds = windowSeconds(startTime, endTime)
        val gaps = count - 1
        val exactIntervalSeconds = totalSeconds.toDouble() / gaps.toDouble()
        val actualInterval = DayTime.createWithSeconds(exactIntervalSeconds.roundToInt())

        val startSeconds = startTime.inWholeSeconds
        val daySeconds = 24 * 3600

        val slots = List(count) { i ->
            val time = when (i) {
                0 -> startTime
                count - 1 -> endTime
                else -> {
                    // Independent rounding per slot avoids error accumulation.
                    val absSeconds = startSeconds + (i * exactIntervalSeconds).roundToInt()
                    DayTime.createWithSeconds(absSeconds % daySeconds)
                }
            }
            val amount = if (i == count - 1) {
                // Adjust last slot so the total equals goalAmountMl exactly.
                goalAmountMl.toFloat() - (gaps * baseAmountPerDrinkMl)
            } else {
                baseAmountPerDrinkMl
            }
            DrinkSlot(time = time, amountMl = amount)
        }

        return DrinkSchedule(slots = slots, actualInterval = actualInterval, count = count)
    }

    private fun windowSeconds(startTime: DayTime, endTime: DayTime): Int =
        DayTime.calculateTimeDifference(startTime, endTime).inWholeSeconds
}
