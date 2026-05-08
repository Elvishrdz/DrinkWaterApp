package com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo

import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.calculator.calculateNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.drinkalarm.simulator.simulateNextIntervalCalculation
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

/**
 * Pure decision function extracted from DrinkAlarmManager.whatShouldIDoNow().
 * All inputs are passed explicitly so this can be unit-tested without Android or coroutine setup.
 *
 * @param now            Current device wall-clock time.
 * @param startTime      Start of the user's active drinking window (today's date, pref hour/minute).
 * @param endTime        End of the user's active drinking window (today's date, pref hour/minute).
 * @param hasActiveAlarm    Whether a pending alarm is currently registered with AlarmManager.
 * @param alarmAlarmTriggersAt  Wall-clock fire time of the stored alarm on today's date, or null if none.
 */
internal fun decideWhatToDoNow(
    now: LocalDateTime,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    hasActiveAlarm: Boolean,
    // we can receive this value even if no active alarm exists
    // it can be a cached time for a previously existing alarm.
    alarmAlarmTriggersAt: LocalDateTime?,
    interval: AlarmInterval = AlarmInterval(0, 0), // TODO: fix tests with this
    goalMl: Float = 1f, // TODO: fix tests with this
): WhatToDoNow {
    val undefinedActiveHours = startTime == now && endTime == now
    val invalidActiveHours = startTime == endTime

    if (undefinedActiveHours || invalidActiveHours) {
        // without active hours we cant do anything
        return WhatToDoNow.InvalidActiveHours
    }

    fun invokeNextAlarmTime() = getNextAlarmSlot(
        now = now,
        startTime = startTime,
        endTime = endTime,
        goalMl = goalMl,
        interval = interval,
    )

    val isInActiveTime = now in startTime..endTime

    val inActiveHoursDecider: () -> WhatToDoNow = {
        if (hasActiveAlarm) {
            val hasCachedAlarm = alarmAlarmTriggersAt != null
            val nextAlarmTime = invokeNextAlarmTime()

            if (hasCachedAlarm) {
                // with a cached alarm:
                // is in the past: remind now
                // is in the future:
                // alarm interval is correct: do nothing
                // alarm interval is incorrect: reschedule alarm

                val cachedAlarmIsInThePast = alarmAlarmTriggersAt < now

                if (cachedAlarmIsInThePast) {
                    // alarm time is in the past and we are in active hours
                    WhatToDoNow.RemindNow
                } else {
                    // alarm time is in the future, and we are in active hours
                    val isAtExpectedTime = alarmAlarmTriggersAt == nextAlarmTime

                    if (isAtExpectedTime) {
                        WhatToDoNow.DoNothing
                    } else {
                        // alarm has wrong time and needs to be rescheduled
                        WhatToDoNow.RescheduleInterval(
                            wrong = alarmAlarmTriggersAt,
                            correct = nextAlarmTime,
                        )
                    }
                }

            } else {
                // alarm is set, but no cached: reschedule the next alarm
                WhatToDoNow.RescheduleInterval(
                    wrong = null,
                    correct = nextAlarmTime,
                )
            }

        } else {
            // no alarm scheduled in the system
            val hasCachedAlarm = alarmAlarmTriggersAt != null
            val nextAlarmTime = invokeNextAlarmTime()

            if (hasCachedAlarm) {
                // no alarm but there is a cached value: restore
                val cachedAlarmIsInThePast = alarmAlarmTriggersAt < now

                if (cachedAlarmIsInThePast) {
                    WhatToDoNow.RemindNow

                } else {
                    // always reschedule alarm to create cached alarm
                    val isAtExpectedTime = alarmAlarmTriggersAt == nextAlarmTime

                    WhatToDoNow.RescheduleInterval(
                        wrong = if (isAtExpectedTime) null else alarmAlarmTriggersAt,
                        correct = nextAlarmTime,
                    )
                }

            } else {
                // no alarm and no cache: reschedule next interval
                WhatToDoNow.RescheduleInterval(
                    wrong = null,
                    correct = nextAlarmTime,
                )
            }
        }
    }

    val inUnactiveHoursDecider: () -> WhatToDoNow = {
        if (hasActiveAlarm) {
            val hasCachedAlarm = alarmAlarmTriggersAt != null
            val nextAlarmTime = invokeNextAlarmTime()

            if (hasCachedAlarm) {
                // outside active hours, and we have a cache alarm value
                val cachedAlarmIsInThePast = alarmAlarmTriggersAt < now

                if (cachedAlarmIsInThePast) {
                    // cached alarm is in the past and we are outside active hours: set next start time
                    WhatToDoNow.RescheduleInterval(
                        wrong = alarmAlarmTriggersAt,
                        correct = nextAlarmTime,
                    )

                } else {
                    // cache alarm is in the future, check if is the correct interval
                    val isAtExpectedTime = alarmAlarmTriggersAt == nextAlarmTime

                    if (isAtExpectedTime) {
                        WhatToDoNow.DoNothing
                    } else {
                        // cache value is wrong, we need to set to the start of next period
                        WhatToDoNow.RescheduleInterval(
                            wrong = alarmAlarmTriggersAt,
                            correct = nextAlarmTime,
                        )
                    }
                }

            } else {
                // we have an alarm in the system, we are outside active hours, and we don't have a cache alarm value
                // reschedule alarm to save new cache
                WhatToDoNow.RescheduleInterval(
                    wrong = null,
                    correct = nextAlarmTime,
                )
            }


        } else {
            // no alarm scheduled in the system, and outside active hours
            val hasCachedAlarm = alarmAlarmTriggersAt != null
            val nextAlarmTime = invokeNextAlarmTime()

            if (hasCachedAlarm) {
                // no alarm but there is a cached value: restore
                val cachedAlarmIsInThePast = alarmAlarmTriggersAt < now

                if (cachedAlarmIsInThePast) {
                    // if is in the past, and outside active hours: reschedule to next interval
                    WhatToDoNow.RescheduleInterval(
                        wrong = alarmAlarmTriggersAt,
                        correct = nextAlarmTime,
                    )

                } else {
                    // cache is in the future, check if is correct
                    val isAtExpectedTime = alarmAlarmTriggersAt == nextAlarmTime

                    if (isAtExpectedTime) {
                        WhatToDoNow.DoNothing

                    } else {
                        WhatToDoNow.RescheduleInterval(
                            wrong = alarmAlarmTriggersAt,
                            correct = nextAlarmTime,
                        )
                    }
                }

            } else {
                // no alarm and no cache: reschedule next interval
                WhatToDoNow.RescheduleInterval(
                    wrong = null,
                    correct = nextAlarmTime,
                )
            }
        }
    }

    return if (isInActiveTime) {
        inActiveHoursDecider()
    } else {
        inUnactiveHoursDecider()
    }
}

// si estamos dentro de horas activas:
// 1. si no esta la alarma y el tiempo ya paso: llamar recordatorio ahora mismo y programar sig. intervalo.
// 2. si no esta la alarma y el tiempo no ha pasado: programar alarma para siguiente intervalo.
// 3. si ya esta la alarma y el tiempo NO esta en el intervalo requerido: programar alarma para el proximo intervalo
// 3. si ya esta la alarma y el tiempo esta en el intervalo requerido: no hacer nada
//
// si estamos fuera de horas activas:
// 1. si no esta la alarma y el tiempo ya paso: programar alarma para inicio de proxima hora activa.
// 2. si no esta la alarma y el tiempo no ha pasado: programar alarma para inicio de proxima hora activa.
// 3. si ya esta la alarma y el tiempo NO esta en el intervalo requerido: programar alarma para inicio de proxima hora activa.
// 4. si ya esta la alarma y el tiempo esta en el intervalo requerido: no hacer nada


fun getNextAlarmSlot(
    now: LocalDateTime,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    goalMl: Float,
    interval: AlarmInterval,
    testMode: Boolean = false,
): LocalDateTime {
    return if (testMode) {
        simulateNextIntervalCalculation(
            now = now,
            startTime = startTime,
            endTime = endTime,
        )
    } else {
        val intervalMinutes = (interval.hour * 60 + interval.minute).coerceAtLeast(1)

        val startDayTime = DayTime(startTime.hour, startTime.minute)
        val endDayTime = DayTime(endTime.hour, endTime.minute)

        calculateNextAlarmSlot(
            now = now,
            startTime = startDayTime,
            endTime = endDayTime,
            goalMl = goalMl,
            intervalMinutes = intervalMinutes,
        )
    }
}
