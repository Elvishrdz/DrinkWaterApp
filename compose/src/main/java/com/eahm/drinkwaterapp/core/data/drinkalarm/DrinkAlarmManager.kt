package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DrinkAlarmManager(
    private val application: Application
) {
    private val _state = MutableStateFlow(getState())

    val state: StateFlow<DrinkAlarmState> = _state

    init {
        update()
    }

    fun setupAlarm(context: Context) {
        val isAlarmActive = AlarmScheduler.isAlarmActive(context, DRINK_ALARM_ID)

        if (isAlarmActive) {
            AlarmScheduler.cancelAlarm(context, DRINK_ALARM_ID)
        }

        val intervalInWholeMillis = getIntervalInWholeMillis(context)

        AlarmScheduler.scheduleAlarm(context, DRINK_ALARM_ID, intervalInWholeMillis)

        AlarmIntervals.clearNextInterval(context)

        update()
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
