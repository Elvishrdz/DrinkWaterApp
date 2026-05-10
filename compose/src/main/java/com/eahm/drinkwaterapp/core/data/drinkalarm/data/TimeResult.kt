package com.eahm.drinkwaterapp.core.data.drinkalarm.data

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class TimeResult(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val millisecond: Long,
) {
    companion object
}

val TimeResult.inWholeMinutes: Int
    get() = hour * 60 + minute

val TimeResult.inWholeSeconds: Int
    get() = hour * 3600 + minute * 60 + second

val TimeResult.inWholeMillis: Long
    get() = inWholeSeconds * 1000L + millisecond

fun TimeResult.Companion.fromTotalMillis(totalMillis: Long): TimeResult {
    val hours = (totalMillis / 3_600_000L).toInt()
    val minutes = ((totalMillis % 3_600_000L) / 60_000L).toInt()
    val seconds = ((totalMillis % 60_000L) / 1000L).toInt()
    val millis = totalMillis % 1000L
    return TimeResult(hour = hours, minute = minutes, second = seconds, millisecond = millis)
}

// TODO: rename to fromExactSeconds
fun TimeResult.Companion.createWithSeconds(seconds: Long): TimeResult =
    fromTotalMillis((seconds * 1000L))

fun TimeResult.Companion.calculateTimeDifference(start: TimeResult, end: TimeResult): TimeResult {
    val startMillis = start.inWholeMillis
    val endMillis = end.inWholeMillis
    val diffMillis = if (endMillis >= startMillis) {
        endMillis - startMillis
    } else {
        (24L * 3_600_000L - startMillis) + endMillis
    }
    return fromTotalMillis(diffMillis)
}

@OptIn(ExperimentalTime::class)
fun TimeResult.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = localDateTime.year,
        month = localDateTime.month,
        day = localDateTime.day,
        hour = hour,
        minute = minute,
        second = second,
        nanosecond = 0,
    )
}
