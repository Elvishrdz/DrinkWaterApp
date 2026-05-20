package com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo

import android.app.Application
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager.Companion.DRINK_ALARM_ID
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.toLocalDateTime
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val whatToDoServiceModule = module {
    singleOf(::WhatToDoService)
}

class WhatToDoService(
    private val application: Application,
    private val prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) {

    @OptIn(ExperimentalTime::class)
    suspend fun whatShouldIDoNow(): WhatToDoNow {
        val currentDeviceDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val interval = prefsStorage.getInterval().firstOrNull()
            ?.let { AlarmInterval(it.hour, it.minute) }
            ?: throw Exception("WhatShouldIDoNow? Getting the interval was not possible")
        val startTime = prefsStorage.getStartTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime
        val endTime = prefsStorage.getEndTime().firstOrNull()?.toLocalDateTime()
            ?: currentDeviceDateTime

        val todayEpoch = currentDeviceDateTime.date.toEpochDays()

        val goalMl = database.getDailyGoalDao()
            .getGoalForDate(todayEpoch)
            .firstOrNull()
            ?.goalMl?.takeIf { it > 0f } ?: DEFAULT_GOAL_ML

        return whatShouldIDo(
            localDateTime = currentDeviceDateTime,
            startTime = startTime,
            endTime = endTime,
            interval = interval,
            goalMl = goalMl,
        )
    }

    @OptIn(ExperimentalTime::class)
    fun whatShouldIDo(
        localDateTime: LocalDateTime,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        interval: AlarmInterval,
        goalMl: Float,
    ): WhatToDoNow {
        val alarmActive = AlarmScheduler.isAlarmActive(application, DRINK_ALARM_ID)

        // cached value, alarm info can exist if the alarm was not triggered
        // for example when installing an update or restarting the phone with a different time
        val alarmInfo = AlarmScheduler.getAlarmStorageInfo(application, DRINK_ALARM_ID)
        val storedAlarmTriggerAt = alarmInfo?.alarmTriggerAt?.toLocalDateTime()

        return decideWhatToDoNow(
            now = localDateTime,
            startTime = startTime,
            endTime = endTime,
            hasActiveAlarm = alarmActive,
            alarmAlarmTriggersAt = storedAlarmTriggerAt,
            interval = interval,
            goalMl = goalMl,
        )
    }
}
