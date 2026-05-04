package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Context
import android.util.Log
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.extension.canShowNotifications
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
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
        if (AlarmScheduler.isAlarmActive(context, DRINK_ALARM_ID)) {
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
                context,
                DRINK_ALARM_ID,
                result.delayMillis
            )

            is NextAlarmResult.BeforeWindow -> AlarmScheduler.scheduleAlarm(
                context,
                DRINK_ALARM_ID,
                result.delayMillis
            )

            is NextAlarmResult.AfterWindow -> AlarmScheduler.scheduleAlarm(
                context,
                DRINK_ALARM_ID,
                result.delayMillis
            )

            is NextAlarmResult.LastDrink -> AlarmScheduler.scheduleAlarm(
                context,
                DRINK_ALARM_ID,
                result.delayMillis
            )

            NextAlarmResult.GoalReached -> { /* alarm already cancelled above; nothing to schedule */
            }
        }

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
            hasNotificationPermissions = application.canShowNotifications,
            isAlarmActive = isAlarmActive,
        )
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, DRINK_ALARM_ID).toString()
    }


    // New
    data class AlarmConfiguration(
        val source: String, // drink button / onboarding / etc
    )

    fun scheduleAlarm(configuration: AlarmConfiguration) {

    }

    fun onResume() {
        Log.i("TESTING_THIS", "onResume: called now")
        update()

        if (application.canShowNotifications) {
            // notification
        }

        if (isAlarmActive) {
            // notification
        }
    }

    companion object {
        private const val DRINK_ALARM_ID = 127
    }
}
