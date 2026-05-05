package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import androidx.compose.ui.util.fastRoundToInt
import com.eahm.drinkwaterapp.core.data.datastore.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.extension.canShowNotifications
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class DrinkAlarmManager(
    private val application: Application,
    private val prefsStorage: PrefsStorage,
    private val drinkStatsObserver: DrinkStatsObserver,
) {
    private val isAlarmActive
        get() = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

    private val _state = MutableStateFlow(getState())

    val state: StateFlow<DrinkAlarmState> = _state

    private val nextAlarmCalculator = NextAlarmCalculator()

    init {
        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setupAlarm(context: Context) {
        return// TODO: remove todo later, is for testing

        if (isAlarmActive) {
            AlarmScheduler.cancelAlarm(context, DRINK_ALARM_ID)
        }

        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val now = DayTime(localDateTime.time.hour, localDateTime.time.minute)

        // TODO: if this fails, the alarms will not trigger again — notify the user.
        val startTime = prefsStorage.getStartTime().firstOrNull()
            ?.let { DayTime(it.hour, it.minute) }
            ?: throw Exception("Alarm can't be rescheduled without start time")

        // TODO: if this fails, the alarms will not trigger again — notify the user.
        val endTime = prefsStorage.getEndTime().firstOrNull()
            ?.let { DayTime(it.hour, it.minute) }
            ?: throw Exception("Alarm can't be rescheduled without end time")

        // TODO: if this fails, the alarms will not trigger again — notify the user.
        val amountPerDrink = prefsStorage.getAmountPerDrink().firstOrNull()
            ?: throw Exception("Alarm can't be rescheduled without amount per drink")

        // TODO: if this fails, the alarms will not trigger again — notify the user.
        val drinkStats = drinkStatsObserver.drinkStats.firstOrNull()
            ?: throw Exception("Alarm can't be rescheduled without drink stats")

        val result = nextAlarmCalculator.calculate(
            now = now,
            startTime = startTime,
            endTime = endTime,
            remainingMl = drinkStats.remainingAmount,
            amountPerDrinkMl = amountPerDrink,
        )

        when (result) {
            is NextAlarmResult.Schedule -> AlarmScheduler.scheduleAlarm(
                context = context,
                alarmId = DRINK_ALARM_ID,
                triggerInTimeFromNowMillis = result.delayMillis,
            )

            is NextAlarmResult.BeforeWindow -> AlarmScheduler.scheduleAlarm(
                context = context,
                alarmId = DRINK_ALARM_ID,
                triggerInTimeFromNowMillis = result.delayMillis,
            )

            is NextAlarmResult.AfterWindow -> AlarmScheduler.scheduleAlarm(
                context = context,
                alarmId = DRINK_ALARM_ID,
                triggerInTimeFromNowMillis = result.delayMillis,
            )

            is NextAlarmResult.LastDrink -> AlarmScheduler.scheduleAlarm(
                context = context,
                alarmId = DRINK_ALARM_ID,
                triggerInTimeFromNowMillis = result.delayMillis,
            )

            NextAlarmResult.GoalReached -> {
                /* alarm already canceled above; nothing to schedule */
            }
        }

        AlarmIntervals.clearNextInterval(context)

        update()
    }

    fun setInterval(hour: Int, minute: Int) {
        val isAlarmActive = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

        if (isAlarmActive) {
            AlarmIntervals.setNextInterval(application, hour, minute)

        } else {
            AlarmIntervals.clearNextInterval(application)
            AlarmIntervals.setInterval(application, hour, minute)
        }

        update()
    }

    private fun update() {
        _state.update { getState() }
    }

    private fun getState(): DrinkAlarmState {
        val alarmStorageInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

        return DrinkAlarmState(
            active = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID),
            interval = AlarmIntervals.getInterval(application),
            nextInterval = AlarmIntervals.getNextInterval(application),
            hasNotificationPermissions = application.canShowNotifications,
            isAlarmActive = isAlarmActive,
            alarmStorageInfo = alarmStorageInfo,
        )
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, DRINK_ALARM_ID).toString()
    }

    fun onResume() {
        if (application.canShowNotifications) {
            // if we can show notifications, then we just need the alarm
        } else {
            // we need the notification permission otherwise users will receive the app reminder
            // display in the app this information to the user.
        }

        if (isAlarmActive) {
            // all good, waiting for the next reminder
        } else {
            // we need to restore the alarm to trigger the reminder
        }

        update()

        if (isAlarmActive) return

        testScheduleAlarm()
    }

    private val testInterval = 7.seconds.inWholeMilliseconds

    fun testScheduleAlarm(configuration: AlarmConfiguration = AlarmConfiguration()) {
        AlarmScheduler.scheduleAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
            triggerInTimeFromNowMillis = testInterval,
        )

        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun restoreAlarms() {
        // se llama muchas veces,
        // restore deberia pasar solo una vez.
        // no se establece ninguna alarma si ya paso el tiempo, y debe ser llamado ahora.
        // en ese caso no se hace nada...

        val whatToDo = whatShouldIDoNow()

        val alarmStorageInfos = AlarmScheduler.getAlarmStorageInfos(application)

        alarmStorageInfos.forEach {
            // TODO: if alarm is in the past, call right away
            val now = Clock.System.now()
            val currentTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val alarmTime = LocalDateTime(
                year = currentTime.year,
                month = currentTime.month,
                day = currentTime.day,
                hour = it.alarmTriggerAt.hour,
                minute = it.alarmTriggerAt.minute,
                second = it.alarmTriggerAt.second,
            )

            val isInThePast = alarmTime < currentTime

            if (isInThePast) {
                // if alarm is in the past, trigger alarm now.
                AlarmScheduler.callBroadcastReceiver(application, DRINK_ALARM_ID)
//                val currentInstant = currentTime.toInstant(TimeZone.currentSystemDefault())
//                val newInstant = currentInstant.plus(testInterval.milliseconds)
//                val newLocalDateTime = newInstant.toLocalDateTime(TimeZone.currentSystemDefault())
//                val totalSeconds = (newLocalDateTime.hour * 3600) +
//                        (newLocalDateTime.minute * 60) +
//                        newLocalDateTime.second
//
//                AlarmTime.createWithSeconds(seconds = totalSeconds)

            } else {
                // use same time
                val time = it.alarmTriggerAt

                val alarmTime2 = LocalDateTime(
                    year = currentTime.year,
                    month = currentTime.month,
                    day = currentTime.day,
                    hour = time.hour,
                    minute = time.minute,
                    second = time.second,
                )

                if (alarmTime2 < currentTime) {
                    throw Exception("Time is still in the past: $alarmTime2 ---- $currentTime")
                }

                AlarmScheduler.scheduleAlarm(
                    context = application,
                    alarmId = it.alarmId,
                    hour = time.hour,
                    minute = time.minute,
                    second = time.second,
                )

            }
        }

        update()
    }

    sealed interface WhatToDoNow {
        data object RemindNow : WhatToDoNow
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun whatShouldIDoNow(): WhatToDoNow {
        val now = Clock.System.now()
        val dateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val startTime = prefsStorage.getStartTime().firstOrNull()?.toLocalDateTime() ?: dateTime
        val endTime = prefsStorage.getEndTime().firstOrNull()?.toLocalDateTime() ?: dateTime

        val isInActiveTime = startTime != endTime && dateTime in startTime..endTime

        return if (isInActiveTime) {
            // check time.
            // si estamos dentro de horas activas:
            // 1. si no esta la alarma y el tiempo ya paso: llamar recordatorio ahora mismo y programar sig. intervalo.
            WhatToDoNow.RemindNow
            // 2. si no esta la alarma y el tiempo no ha pasado: programar alarma para siguiente intervalo.
            // 3. si ya esta la alarma y el tiempo NO esta en el intervalo requerido: programar alarma para el proximo intervalo
            // 3. si ya esta la alarma y el tiempo esta en el intervalo requerido: no hacer nada

            WhatToDoNow.RemindNow

        } else {
            // si estamos fuera de horas activas:
            // 1. si no esta la alarma y el tiempo ya paso: programar alarma para inicio de proxima hora activa.
            // 2. si no esta la alarma y el tiempo no ha pasado: programar alarma para inicio de proxima hora activa.
            // 3. si ya esta la alarma y el tiempo NO esta en el intervalo requerido: programar alarma para inicio de proxima hora activa.
            // 4. si ya esta la alarma y el tiempo esta en el intervalo requerido: no hacer nada

            WhatToDoNow.RemindNow
        }
    }

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

    fun stopAlarms() {
        AlarmScheduler.cancelAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
        )

        update()
    }

    fun refresh() = update()

    fun onAlarmReceived(alarmId: Int) {
        testScheduleAlarm()

        update()
    }

    data class AlarmConfiguration(
        val source: String = "app", // drink button / onboarding / etc
    )

    companion object {
        private const val DRINK_ALARM_ID = 127
    }
}

data class AlarmTime(
    val hour: Int,
    val minute: Int,
    val second: Int,
) {
    companion object
}

fun AlarmTime.Companion.createWithSeconds(seconds: Int): AlarmTime {
    val results = createTimeWithSeconds(seconds)

    return AlarmTime(
        hour = results.hours,
        minute = results.minutes,
        second = results.seconds,
    )
}

fun createTimeWithSeconds(seconds: Int): TimeResult {
    val totalMinutes = seconds.toFloat() / 60
    val roundedTotalMinutes = totalMinutes.fastRoundToInt()
    val hours = ((roundedTotalMinutes / 60) % 24).coerceAtMost(23)
    val minutes = (roundedTotalMinutes % 60).coerceAtMost(59)
    val roundedSeconds = roundedTotalMinutes * 60
    val millis = roundedSeconds * 1000L

    return TimeResult(
        hours = hours,
        minutes = minutes,
        seconds = roundedSeconds,
        millis = millis,
    )
}

data class TimeResult(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val millis: Long,
)
