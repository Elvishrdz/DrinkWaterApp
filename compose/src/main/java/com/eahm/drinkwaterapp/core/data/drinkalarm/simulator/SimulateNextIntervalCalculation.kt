package com.eahm.drinkwaterapp.core.data.drinkalarm.simulator

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun createSimulatedDateTime(hour: Int, minute: Int): LocalDateTime {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(
        year = today.year,
        month = today.month,
        day = today.day,
        hour = hour,
        minute = minute,
    )
}
