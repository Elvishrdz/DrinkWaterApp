package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class DrinkAlarmManager(
    private val application: Application,
    private val prefsStorage: PrefsStorage,
    private val drinkStatsObserver: DrinkStatsObserver,
) {
    private val _state = MutableStateFlow(getState())

    val state: StateFlow<DrinkAlarmState> = _state

    private val calculator = DrinkAlarmCalculator2()

    init {
        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setupAlarm(context: Context) {
        val isAlarmActive = AlarmScheduler.isAlarmActive(context, DRINK_ALARM_ID)

        if (isAlarmActive) {
            AlarmScheduler.cancelAlarm(context, DRINK_ALARM_ID)
        }

        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val startTime = DayTime(localDateTime.time.hour, localDateTime.time.minute)

        // TODO: if this fails, the alarms will not trigger again, notify users to continue alarms.
        val endTime = prefsStorage.getEndTime().firstOrNull()?.let { DayTime(it.hour, it.minute) }
            ?: throw Exception("Alarm can't be rescheduled without end time")

        // TODO: if this fails, the alarms will not trigger again, notify users to continue alarms.
        val amountPerDrink = prefsStorage.getAmountPerDrink().firstOrNull()
            ?: throw Exception("Alarm can't be rescheduled without end time")

        // TODO: if this fails, the alarms will not trigger again, notify users to continue alarms.
        val currentDrinkStats = drinkStatsObserver.drinkStats.firstOrNull()
            ?: throw Exception("Alarm can't be rescheduled without drink stats")

        val goal = currentDrinkStats.goalAmount
        val remaining = currentDrinkStats.remainingAmount

        val schedule = calculator.scheduleByAmount(
            startTime = startTime,
            endTime = endTime,
            goalAmountMl = remaining,
            amountPerDrinkMl = amountPerDrink,
        )

        val nextDrink = schedule.slots.first().time

        val interval = AlarmInterval(nextDrink.hour, nextDrink.minute)

        AlarmScheduler.scheduleAlarm(context, DRINK_ALARM_ID, interval.inWholeMillis())

        AlarmIntervals.clearNextInterval(context)

        update()

        /*
         we need to track:
         1. which is the next alarm
         2. if start/end date changes, we need to update intervals for next day
         3. if we change drink before, we need to calculate new values.

         */


//        val intervalInWholeMillis = getIntervalInWholeMillis(context)
//
//        AlarmScheduler.scheduleAlarm(context, DRINK_ALARM_ID, intervalInWholeMillis)
//
//        AlarmIntervals.clearNextInterval(context)
//
//        update()
    }

    fun stop() {
        AlarmScheduler.cancelAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
        )

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
        return DrinkAlarmState(
            active = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID),
            interval = AlarmIntervals.getInterval(application),
            nextInterval = AlarmIntervals.getNextInterval(application),
        )
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, DRINK_ALARM_ID).toString()
    }

    private fun getIntervalInWholeMillis(context: Context): Long {
        return (AlarmIntervals.getNextInterval(context) ?: AlarmIntervals.getInterval(context))
            .inWholeMillis()
    }

    private fun AlarmInterval.inWholeMillis(): Long {
        return hour.hours.inWholeMilliseconds + minute.minutes.inWholeMilliseconds
    }

    companion object {
        private const val DRINK_ALARM_ID = 127
    }
}
