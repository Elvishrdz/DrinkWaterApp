package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.calculateTimeDifference
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.fromTotalMillis
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.inWholeMillis
import com.eahm.drinkwaterapp.core.domain.extension.createTimeResultFromSeconds
import kotlin.math.roundToInt

data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int,
) {
    companion object
}

val Time.inWholeMinutes
    get() = (hour * 60) + minute

val Time.inWholeSeconds
    get() = inWholeMinutes * 60 + second

fun DayTime.toTime(): Time {
    return Time(hour, minute, 0)
}

fun Time.Companion.createWithSeconds(seconds: Int): Time {
    val results = seconds.createTimeResultFromSeconds()

    return Time(
        hour = results.hour,
        minute = results.minute,
        second = results.second,
    )
}

fun Time.Companion.calculateTimeDifference(
    start: Time,
    end: Time,
): Time {
    val startSeconds = start.inWholeSeconds
    val endSeconds = end.inWholeSeconds

    val diffSeconds = if (endSeconds >= startSeconds) {
        endSeconds - startSeconds
    } else {
        (24 * 3600 - startSeconds) + endSeconds
    }

    return Time.createWithSeconds(diffSeconds)
}


data class DrinkSlot(
    val time: TimeResult,
    val amountMl: Float,
)

data class DrinkSchedule(
    val slots: List<DrinkSlot>,
    /** Actual interval between consecutive drinks (computed from exact distribution). */
    val actualInterval: TimeResult,
    val count: Int,
)

/**
 * Improved drink schedule calculator that distributes drink times uniformly
 * between [startTime] and [endTime].
 *
 * Contract:
 * - First slot is always at [startTime], last slot is always at [endTime].
 * - N drinks produce N-1 equal gaps — no slot ever exceeds [endTime].
 * - Middle slots are placed at `startTime + i * (totalTime / (N-1))` rounded to the nearest millisecond.
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
        goalAmountMl: Float,
        amountPerDrinkMl: Float,
    ): DrinkSchedule {
        require(amountPerDrinkMl > 0f) { "amountPerDrinkMl must be positive" }
        require(goalAmountMl > 0) { "goalAmountMl must be positive" }

        val count = (goalAmountMl / amountPerDrinkMl).roundToInt().coerceAtLeast(1)
        return buildSchedule(
            startTime = startTime.toTimeResult(),
            endTime = endTime.toTimeResult(),
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
        startTime: TimeResult,
        endTime: TimeResult,
        goalAmountMl: Float,
        desiredIntervalMinutes: Int,
    ): DrinkSchedule {
        require(desiredIntervalMinutes > 0) { "desiredIntervalMinutes must be positive" }
        require(goalAmountMl > 0) { "goalAmountMl must be positive" }

        val totalMillis = windowMillis(startTime, endTime)
        val desiredIntervalMillis = desiredIntervalMinutes * 60_000L

        // gaps = N-1 for N drinks; find the integer gap count closest to the desired interval.
        // roundToInt() rounds ties toward +∞ → prefers more gaps = more drinks.
        val gapsFloat = totalMillis.toFloat() / desiredIntervalMillis.toFloat()
        val gaps = gapsFloat.roundToInt().coerceAtLeast(1)
        val count = gaps + 1

        val baseAmountPerDrink = goalAmountMl / count.toFloat()
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
        startTime: TimeResult,
        endTime: TimeResult,
        count: Int,
        goalAmountMl: Float,
        baseAmountPerDrinkMl: Float,
    ): DrinkSchedule {
        // Edge case: single drink — no meaningful interval, no endTime pin.
        if (count == 1) {
            return DrinkSchedule(
                slots = listOf(DrinkSlot(time = startTime, amountMl = goalAmountMl)),
                actualInterval = TimeResult(0, 0, 0, 0L),
                count = 1,
            )
        }

        val totalMillis = windowMillis(startTime, endTime)
        val gaps = count - 1
        val exactIntervalMillis = totalMillis.toDouble() / gaps.toDouble()
        val actualInterval = TimeResult.fromTotalMillis(exactIntervalMillis.toLong())

        val startMillis = startTime.inWholeMillis
        val dayMillis = 24L * 3_600_000L

        val slots = List(count) { i ->
            val time = when (i) {
                0 -> startTime
                count - 1 -> endTime
                else -> {
                    // Independent rounding per slot avoids error accumulation.
                    val absMillis = startMillis + (i * exactIntervalMillis).toLong()
                    TimeResult.fromTotalMillis(absMillis % dayMillis)
                }
            }
            val amount = if (i == count - 1) {
                // Adjust last slot so the total equals goalAmountMl exactly.
                goalAmountMl - (gaps * baseAmountPerDrinkMl)
            } else {
                baseAmountPerDrinkMl
            }
            DrinkSlot(time = time, amountMl = amount)
        }

        return DrinkSchedule(slots = slots, actualInterval = actualInterval, count = count)
    }

    private fun windowMillis(startTime: TimeResult, endTime: TimeResult): Long =
        TimeResult.calculateTimeDifference(startTime, endTime).inWholeMillis
}
