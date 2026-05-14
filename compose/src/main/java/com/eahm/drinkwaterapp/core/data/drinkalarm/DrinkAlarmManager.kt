package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Intent
import android.util.Log
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmConfiguration
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoService
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.getNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.canScheduleExactAlarms
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class DrinkAlarmManager(
    private val application: Application,
    private val prefsStorage: PrefsStorage,
    private val database: AppDatabase,
    private val whatToDoService: WhatToDoService,
) {
    private val _state = MutableStateFlow(getState())

    val state = combine(
        prefsStorage.getInterval(),
        _state,
    ) { interval, state ->
        state.copy(
            interval = AlarmInterval(interval.hour, interval.minute),
        )
    }

    private val isAlarmActive get() = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

    private val restoreAlarmMutex = Mutex()

    init {
        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setupAlarm(source: AlarmSource) {
        val currentDeviceDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val interval = prefsStorage.getInterval().firstOrNull()
            ?.let { AlarmInterval(it.hour, it.minute) }
            ?: throw Exception("Getting the interval was not possible. Source: $source")
        val startTime = prefsStorage.getStartTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime
        val endTime = prefsStorage.getEndTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime

        val todayEpoch = currentDeviceDateTime.date.toEpochDays()
        val goalMl = database.getDailyGoalDao()
            .getGoalForDate(todayEpoch)
            .firstOrNull()
            ?.goalMl ?: DEFAULT_GOAL_ML

        val nextSlotTime = getNextAlarmSlot(
            now = currentDeviceDateTime,
            startTime = startTime,
            endTime = endTime,
            goalMl = goalMl,
            interval = interval,
        )

        val configuration = AlarmConfiguration(
            source = source,
            schedule = AlarmSchedule.AtExactTime(
                hour = nextSlotTime.hour,
                minute = nextSlotTime.minute,
                second = 0,
            ),
        )

        scheduleDrinkAlarm(configuration)
    }

    fun setupTestAlarm(source: AlarmSource) {
        val configuration = AlarmConfiguration(
            source = source,
            schedule = AlarmSchedule.AfterGivenTime(
                milliseconds = testInterval,
            ),
        )

        scheduleDrinkAlarm(configuration)

        update()
    }

    @OptIn(ExperimentalTime::class)
    private fun scheduleDrinkAlarm(configuration: AlarmConfiguration) {
        val alarmId = DRINK_ALARM_ID

        when (val data = configuration.schedule) {
            is AlarmSchedule.AtExactTime -> {
                AlarmScheduler.scheduleAlarm(
                    context = application,
                    alarmId = alarmId,
                    hour = data.hour,
                    minute = data.minute,
                    second = data.second,
                )
            }

            is AlarmSchedule.AfterGivenTime -> {
                AlarmScheduler.scheduleAlarm(
                    context = application,
                    alarmId = alarmId,
                    millisToTriggerFromNow = testInterval,
                )
            }
        }

        update()
    }

    suspend fun determineAction(source: AlarmSource) {
        askWhatToDo(source.toString())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun onAlarmReceived(source: AlarmSource) {
        // always schedule next alarm when previous is received
        // FIXME: alarma recibida, revisar cual es el siguiente intervalo y programar alarma.
        setupAlarm(source)

        val alarmStorageInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

        Log.i("DRINK_ALARM_MANAGER", "alarm received, next alarm at: $alarmStorageInfo")
    }

    suspend fun onResume() {
        update()

        askWhatToDo("onResume")
    }

    @OptIn(ExperimentalTime::class)
    suspend fun restoreAlarm(source: String = "unknown") {
        Log.i("DRINK_ALARM_MANAGER", "restoreAlarms: Source: $source - waiting for lock")
        restoreAlarmMutex.withLock {
            Log.i("DRINK_ALARM_MANAGER", "restoreAlarms: Source: $source - lock acquired")
            askWhatToDo(source)
            update()
        }
    }

    suspend fun setInterval(hour: Int, minute: Int) {
        prefsStorage.setInterval(ActiveTime(hour, minute))
        update()
    }

    fun update() {
        _state.update { getState() }
    }

    private fun getState(interval: AlarmInterval = AlarmInterval(0, 0)): DrinkAlarmState {
        val alarmStorageInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

        return DrinkAlarmState(
            active = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID),
            interval = interval,
            canScheduleExactAlarms = application.canScheduleExactAlarms,
            hasNotificationPermissions = application.canShowNotifications,
            isAlarmActive = isAlarmActive,
            alarmStorageInfo = alarmStorageInfo,
        )
    }

    suspend fun askWhatToDo(source: String = "unknown") {
        Log.i("DRINK_ALARM_MANAGER", "what to do? source: $source")

        when (val data = whatToDoService.whatShouldIDoNow()) {
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

                val intent = Intent(AlarmReceiver.ALARM_ACTION).apply {
                    setPackage(application.packageName)
                }
                application.sendBroadcast(intent)
            }

            is WhatToDoNow.RescheduleInterval -> {
                Log.i("DRINK_ALARM_MANAGER", "what to do? reschedule to interval: $data")

                val configuration = AlarmConfiguration(
                    source = AlarmSource.CustomSource(source),
                    schedule = AlarmSchedule.AtExactTime(
                        hour = data.correct.hour,
                        minute = data.correct.minute,
                        second = data.correct.second,
                    ),
                )

                scheduleDrinkAlarm(configuration)

                update()
            }
        }
    }

    fun clear() {
        stopAlarm()
        NotificationService.cancelDrinkNotification(application)
    }

    fun stopAlarm() {
        AlarmScheduler.cancelAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
        )

        update()
    }

    companion object {
        internal const val DRINK_ALARM_ID = 127

        private val testInterval = 7.seconds.inWholeMilliseconds
    }
}
