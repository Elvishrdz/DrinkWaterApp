package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DrinkAlarmManager(
    private val application: Application
) {
    var active by mutableStateOf(false)
    var interval: Duration? by mutableStateOf(null)

    val drinkAlarmId = 127
    val drinkAlarmInterval = 10.seconds

    init {
        update()
    }

    fun start(context: Context) {
        val (hour, minute) = AlarmIntervals.getInterval(context)
        val inWholeMillis = hour.hours.inWholeMilliseconds + minute.minutes.inWholeMilliseconds

        AlarmScheduler.scheduleAlarm(
            context = context,
            alarmId = drinkAlarmId,
            intervalMs = inWholeMillis,
        )
        update()
    }

    fun stop() {
        AlarmScheduler.cancelAlarm(
            context = application,
            alarmId = drinkAlarmId,
        )

        update()
    }

    private fun update() {
        active = AlarmScheduler.isAlarmActive(application, drinkAlarmId)
        interval = if (active) drinkAlarmInterval else null
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, drinkAlarmId).toString()
    }
}
