package com.eahm.drinkwaterapp.core.data.drinkalarm.calculator

import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.inWholeSeconds
import kotlinx.datetime.LocalDateTime

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
    startTime: DayTime,
    endTime: DayTime,
    goalMl: Float,
    intervalMinutes: Int,
): LocalDateTime {
    val schedule = DrinkAlarmCalculator2().scheduleByInterval(
        startTime = startTime,
        endTime = endTime,
        goalAmountMl = goalMl,
        desiredIntervalMinutes = intervalMinutes.coerceAtLeast(1),
    )

    val nowSeconds = now.hour * 3600 + now.minute * 60 + now.second
    val nextSlot = schedule.slots.firstOrNull { it.time.inWholeSeconds > nowSeconds }
    val target = nextSlot?.time ?: startTime

    return LocalDateTime(
        year = now.year,
        month = now.month,
        day = now.day,
        hour = target.hour,
        minute = target.minute,
    )
}
