package com.eahm.drinkwaterapp.core.domain.extension

import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun AlarmTime.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = localDateTime.year,
        month = localDateTime.month,
        day = localDateTime.day,
        hour = hour,
        minute = minute,
        second = second,
    )
}

fun AlarmTime.Companion.createWithSeconds(seconds: Int): AlarmTime {
    val results = seconds.createTimeResultFromSeconds()

    return AlarmTime(
        hour = results.hour,
        minute = results.minute,
        second = results.second,
    )
}
