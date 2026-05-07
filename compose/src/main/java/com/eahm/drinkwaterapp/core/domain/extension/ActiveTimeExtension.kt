package com.eahm.drinkwaterapp.core.domain.extension

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun ActiveTime.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    val now = Clock.System.now()
    val localDateTime = now.toLocalDateTime(timeZone)

    return LocalDateTime(
        year = localDateTime.year,
        month = localDateTime.month,
        day = localDateTime.day,
        hour = this.hour,
        minute = this.minute,
    )
}
