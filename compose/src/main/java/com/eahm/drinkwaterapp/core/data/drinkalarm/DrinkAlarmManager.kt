package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DrinkAlarmManager(
    private val application: Application
) {
    private val _state = MutableStateFlow(
        DrinkAlarmState(false, null, "")
    )

    val state: StateFlow<DrinkAlarmState> = _state

    init {
        update()
    }

    fun setupAlarm(context: Context) {
        val exists = AlarmScheduler.isAlarmActive(context, DRINK_ALARM_ID)

        if (exists) {
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

    private fun update() {
        val active = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

        val intervalInWholeMillis = getIntervalInWholeMillis(application)

        _state.update {
            it.copy(
                active = active,
                interval = if (active) intervalInWholeMillis else null
            )
        }
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, DRINK_ALARM_ID).toString()
    }

    private fun getIntervalInWholeMillis(context: Context): Long {
        val nextIntervals = AlarmIntervals.getNextInterval(context)

        val (hour, minute) = nextIntervals ?: AlarmIntervals.getInterval(context)

        return hour.hours.inWholeMilliseconds + minute.minutes.inWholeMilliseconds
    }

    companion object {
        private const val DRINK_ALARM_ID = 127
    }
}
