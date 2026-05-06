package com.eahm.drinkwaterapp.core.data.drinkalarm

import android.app.Application
import android.content.Intent
import android.util.Log
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmConfiguration
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkalarm.simulator.simulateNextIntervalCalculation
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.decideWhatToDoNow
import com.eahm.drinkwaterapp.core.domain.extension.canScheduleExactAlarms
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
) {
    private val _state = MutableStateFlow(getState())

    val state: StateFlow<DrinkAlarmState> = _state

    private val isAlarmActive get() = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

    private val restoreAlarmMutex = Mutex()

    init {
        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setupAlarm(source: AlarmSource) {
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

        val configuration = AlarmConfiguration(
            source = source,
            schedule = AlarmSchedule.AtExactTime(
                hour = nextInterval.hour,
                minute = nextInterval.minute,
                second = nextInterval.second,
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
                    triggerInTimeFromNowMillis = testInterval,
                )
            }
        }

        update()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun onAlarmReceived(source: AlarmSource) {
        // always schedule next alarm when previous is received
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

    fun update() {
        _state.update { getState() }
    }

    private fun getState(): DrinkAlarmState {
        val alarmStorageInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)

        return DrinkAlarmState(
            active = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID),
            interval = AlarmIntervals.getInterval(application),
            nextInterval = AlarmIntervals.getNextInterval(application),
            canScheduleExactAlarms = application.canScheduleExactAlarms,
            hasNotificationPermissions = application.canShowNotifications,
            isAlarmActive = isAlarmActive,
            alarmStorageInfo = alarmStorageInfo,
        )
    }

    fun remainingTime(): String {
        return AlarmScheduler.getTimeUntilNextTrigger(application, DRINK_ALARM_ID).toString()
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

                val intent = Intent(AlarmReceiver.ALARM_ACTION).apply {
                    setPackage(application.packageName)
                }
                application.sendBroadcast(intent)
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

    fun stopAlarm() {
        AlarmScheduler.cancelAlarm(
            context = application,
            alarmId = DRINK_ALARM_ID,
        )

        update()
    }

    companion object {
        private const val DRINK_ALARM_ID = 127

        private val testInterval = 7.seconds.inWholeMilliseconds
    }
}
