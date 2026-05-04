package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.extension.canShowNotifications
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler.asDayTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
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
    fun restoreAlarms() {
        val alarmStorageInfos = AlarmScheduler.getAlarmStorageInfos(application)

        alarmStorageInfos.forEach {
            // TODO: if alarm is in the past, recalculate new time
            val now = Clock.System.now()
            val currentTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val alarmTime = LocalDateTime(
                year = currentTime.year,
                month = currentTime.month,
                day = currentTime.day,
                hour = it.alarmTriggerAt.hour,
                minute = it.alarmTriggerAt.minute,
            )

            val isInThePast = alarmTime < currentTime

            val time = if (isInThePast) {
                // TODO: to the current time add a new interval value, sum at millis level
                val currentInstant = currentTime.toInstant(TimeZone.currentSystemDefault())
                val newTime = currentInstant.plus(testInterval.milliseconds)
                val triggerAt = newTime.toEpochMilliseconds()
                triggerAt.asDayTime

            } else {
                // use same time
                it.alarmTriggerAt
            }

            val alarmTime2 = LocalDateTime(
                year = currentTime.year,
                month = currentTime.month,
                day = currentTime.day,
                hour = time.hour,
                minute = time.minute,
            )

            if (alarmTime2 < currentTime) {
                throw Exception("Time is still in the past")
            }

            AlarmScheduler.scheduleAlarm(
                context = application,
                alarmId = it.alarmId,
                hour = time.hour,
                minute = time.minute,
            )
        }

        update()
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
