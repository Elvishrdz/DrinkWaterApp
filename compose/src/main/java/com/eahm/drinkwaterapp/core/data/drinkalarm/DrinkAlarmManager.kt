package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastRoundToInt
import com.eahm.drinkwaterapp.core.data.datastore.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.WhatToDoNow
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.decideWhatToDoNow
import com.eahm.drinkwaterapp.core.data.drinkalarm.simulator.simulateNextIntervalCalculation
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

    suspend fun onResume() {
        update()

        askWhatToDo("onResume")
    }

    private val testInterval = 7.seconds.inWholeMilliseconds

    fun testScheduleAlarm(configuration: AlarmConfiguration = AlarmConfiguration()) {
        return
        AlarmScheduler.scheduleAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
            triggerInTimeFromNowMillis = testInterval,
        )

        update()
    }

    suspend fun askWhatToDo(source: String = "unknown") {
        Log.i("DRINK_ALARM_MANAGER", "what to do? source: $source")

        when (val data = whatShouldIDoNow()) {
            WhatToDoNow.InvalidActiveHours -> {
                Log.i(
                    "DRINK_ALARM_MANAGER",
                    "what to do? we have invalid active hours"
                )
            }

            WhatToDoNow.DoNothing -> {
                Log.i("DRINK_ALARM_MANAGER", "what to do? do nothing")
            }

            WhatToDoNow.RemindNow -> {
                Log.i("DRINK_ALARM_MANAGER", "what to do? remind now")

                // trigger broadcast now
            }

            is WhatToDoNow.RescheduleInterval -> {
                Log.i("DRINK_ALARM_MANAGER", "what to do? reschedule to interval: $data")

                AlarmScheduler.scheduleAlarm(
                    context = application,
                    alarmId = DRINK_ALARM_ID,
                    hour = data.correct.hour,
                    minute = data.correct.minute,
                    second = data.correct.second,
                )

                val alarmStorageInfo =
                    AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

                Log.i("DRINK_ALARM_MANAGER", "we have this stored: $alarmStorageInfo")

                update()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun restoreAlarm(source: String = "unknown") {
        Log.i("DRINK_ALARM_MANAGER", "restoreAlarms: Source: $source")
        askWhatToDo("restoreAlarm")
        update()
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun whatShouldIDoNow(): WhatToDoNow {
        val currentDeviceDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val startTime = prefsStorage.getStartTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime
        val endTime = prefsStorage.getEndTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime

        val alarmActive = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

        // cached value, alarm info can exist if the alarm was not triggered
        // for example when installing an update or restarting the phone with a different time
        val alarmInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)
        val storedAlarmTriggerAt = alarmInfo?.alarmTriggerAt?.toLocalDateTime()

        return decideWhatToDoNow(
            now = currentDeviceDateTime,
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = alarmActive,
            alarmAlarmTriggersAt = storedAlarmTriggerAt,
        )
    }

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

    @OptIn(ExperimentalTime::class)
    suspend fun onAlarmReceived(alarmId: Int) {
        // always schedule next alarm when previous is received
        val currentDeviceDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val startTime = prefsStorage.getStartTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime
        val endTime = prefsStorage.getEndTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime

        val nextInterval = simulateNextIntervalCalculation(
            now = currentDeviceDateTime,
            startTime = startTime,
            endTime = endTime,
        )

        AlarmScheduler.scheduleAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
            hour = nextInterval.hour,
            minute = nextInterval.minute,
            second = nextInterval.second,
        )

        val alarmStorageInfo =
            AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

        Log.i("DRINK_ALARM_MANAGER", "alarm received, next alarm at: $alarmStorageInfo")

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
