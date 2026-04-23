package com.eahm.drinkwaterapp.core.domain.extension

import kotlinx.datetime.LocalDateTime


val LocalDateTime.toStartOfDayTime
    get() = LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )

val LocalDateTime.toEndOfDayTime
    get() = LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = 23,
        minute = 59,
        second = 59,
        nanosecond = 999999999,
    )
