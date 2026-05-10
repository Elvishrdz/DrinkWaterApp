package com.eahm.drinkwaterapp.core.data.drinkalarm.calculator

import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.inWholeMillis
import kotlinx.datetime.LocalDateTime

// TODO: return time in hours:mins:seconds  example: 11:30:10
// handle: 11:30:59 should be 11:40
// handle: 11:30:1 should be 11:40
// handle: 11:30:59 should be 11:40


/**
 * Calculates the next alarm slot time using the real schedule produced by
 * [DrinkAlarmCalculator2.scheduleByInterval].
 *
 * - Finds the first slot strictly after [now] (exclusive at the boundary).
 * - Falls back to [startTime] when [now] is past all slots; the caller's
 *   AlarmScheduler will push a past-today time to tomorrow automatically.
 */
fun calculateNextAlarmSlot(
    now: LocalDateTime,
    startTime: TimeResult,
    endTime: TimeResult,
    goalMl: Float,
    intervalMinutes: Int,
): LocalDateTime {
    val schedule = DrinkAlarmCalculator2().scheduleByInterval(
        startTime = startTime,
        endTime = endTime,
        goalAmountMl = goalMl,
        desiredIntervalMinutes = intervalMinutes.coerceAtLeast(1),
    )

    val nowMillis = (now.hour * 3600L + now.minute * 60L + now.second) * 1000L +
            now.nanosecond / 1_000_000L
    val nextSlot = schedule.slots.firstOrNull { it.time.inWholeMillis > nowMillis }
    val target = nextSlot?.time ?: startTime

    return LocalDateTime(
        year = now.year,
        month = now.month,
        day = now.day,
        hour = target.hour,
        minute = target.minute,
        second = target.second,
        nanosecond = (target.millisecond * 1_000_000L).toInt(),
    )
}
