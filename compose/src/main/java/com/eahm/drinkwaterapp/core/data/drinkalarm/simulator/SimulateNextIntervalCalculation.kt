package com.eahm.drinkwaterapp.core.data.drinkalarm.simulator

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * return an interval between:
 * a) startTime
 * b) 10:00
 * c) 15:00
 * d) endTime
 * */
@Deprecated("Use calculateNextAlarmSlot() instead")
fun simulateNextIntervalCalculation(
    now: LocalDateTime,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
): LocalDateTime {
    val simulatedOptionB = getSimulatedOptionB(now)
    val simulatedOptionC = getSimulatedOptionC(now)

    val nextInterval = when (now) {
        in startTime..<simulatedOptionB -> simulatedOptionB

        in simulatedOptionB..<simulatedOptionC -> simulatedOptionC

        in simulatedOptionC..<endTime -> endTime

        else -> startTime
    }

    return nextInterval
}

@OptIn(ExperimentalTime::class)
fun getSimulatedOptionB(now: LocalDateTime): LocalDateTime {
    return LocalDateTime(
        year = now.year,
        month = now.month,
        day = now.day,
        hour = 10,
        minute = 0,
    )
}

@OptIn(ExperimentalTime::class)
fun getSimulatedOptionC(now: LocalDateTime): LocalDateTime {
    return LocalDateTime(
        year = now.year,
        month = now.month,
        day = now.day,
        hour = 15,
        minute = 0,
    )
}

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
