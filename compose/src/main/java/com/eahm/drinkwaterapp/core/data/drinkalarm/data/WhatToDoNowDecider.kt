package com.eahm.drinkwaterapp.core.data.drinkalarm.data

import kotlinx.datetime.LocalDateTime

/**
 * Pure decision function extracted from DrinkAlarmManager.whatShouldIDoNow().
 * All inputs are passed explicitly so this can be unit-tested without Android or coroutine setup.
 *
 * @param now       Current device wall-clock time.
 * @param startTime      Start of the user's active drinking window (today's date, pref hour/minute).
 * @param endTime        End of the user's active drinking window (today's date, pref hour/minute).
 * @param alarmActive    Whether a pending alarm is currently registered with AlarmManager.
 * @param storedTrigger  Wall-clock fire time of the stored alarm on today's date, or null if none.
 */
internal fun decideWhatToDoNow(
    now: LocalDateTime,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    alarmActive: Boolean,
    storedTrigger: LocalDateTime?,
): WhatToDoNow {
    val isInActiveTime = startTime != endTime && now in startTime..endTime

    return if (isInActiveTime) {
        if (!alarmActive) {
            val timeAlreadyPassed = storedTrigger != null && storedTrigger < now
            if (timeAlreadyPassed) WhatToDoNow.RemindNow else WhatToDoNow.ScheduleNextInterval
        } else {
            val isAtExpectedInterval = storedTrigger != null
                && storedTrigger >= now
                && storedTrigger <= endTime
            if (isAtExpectedInterval) WhatToDoNow.DoNothing else WhatToDoNow.RescheduleInterval
        }
    } else {
        if (!alarmActive) {
            WhatToDoNow.ScheduleForActiveStart
        } else {
            val isScheduledForStart = storedTrigger != null
                && storedTrigger.hour == startTime.hour
                && storedTrigger.minute == startTime.minute
            if (isScheduledForStart) WhatToDoNow.DoNothing else WhatToDoNow.ScheduleForActiveStart
        }
    }
}
