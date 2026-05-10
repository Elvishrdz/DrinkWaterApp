package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.domain.extension.createTimeResultFromSeconds

data class DayTime(
    val hour: Int,
    val minute: Int,
) {
    companion object
}

val DayTime.inWholeMinutes
    get() = (hour * 60) + minute

val DayTime.inWholeSeconds
    get() = inWholeMinutes * 60

fun DayTime.Companion.create(minutes: Int): DayTime {
    return DayTime(
        hour = minutes / 60,
        minute = minutes % 60,
    )
}

fun DayTime.Companion.createWithSeconds(seconds: Int): DayTime {
    val results = seconds.createTimeResultFromSeconds()

    return DayTime(
        hour = results.hour,
        minute = results.minute,
    )
}

fun DayTime.toTimeResult(): TimeResult = TimeResult(hour, minute, 0, 0L)

fun DayTime.Companion.calculateTimeDifference(
    start: DayTime,
    end: DayTime
): DayTime {
    val startMinutes = start.inWholeMinutes
    val endMinutes = end.inWholeMinutes

    val diffMinutes = if (endMinutes >= startMinutes) {
        endMinutes - startMinutes
    } else {
        (24 * 60 - startMinutes) + endMinutes
    }

    return DayTime.create(minutes = diffMinutes)
}
