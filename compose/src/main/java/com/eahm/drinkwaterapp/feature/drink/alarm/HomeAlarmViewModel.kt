package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.getNextAlarmSlot
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.ClockGranularity
import com.eahm.drinkwaterapp.feature.drink.ClockTickObserver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime

internal class HomeAlarmViewModel(
    val drinkAlarmManager: DrinkAlarmManager,
    clockTickObserver: ClockTickObserver,
    private val notificationPermissionObserver: NotificationPermissionObserver,
    private val prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) : ViewModel() {

    private val calculator = DrinkAlarmCalculator2()

    private val alarmPrefs = combine(
        prefsStorage.getInterval(),
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { interval, start, end -> Triple(interval, start, end) }

    val state = combine(
        drinkAlarmManager.state,
        notificationPermissionObserver.hasPermission,
        clockTickObserver.observe(ClockGranularity.MINUTE),
        alarmPrefs,
    ) { drinkAlarmState, hasPermission, now, prefs ->
        if (!hasPermission) return@combine HomeAlarmState.NoPermission

        val (intervalPref, startTimePref, endTimePref) = prefs
        val startTime = startTimePref.toLocalDateTime()
        val endTime = endTimePref.toLocalDateTime()
        val alarmInterval = AlarmInterval(intervalPref.hour, intervalPref.minute)

        if (!drinkAlarmState.isAlarmActive) return@combine HomeAlarmState.NotConfigured

        val goalMl = database.getDailyGoalDao()
            .getGoalForDate(now.date.toEpochDays())
            .firstOrNull()?.goalMl ?: DEFAULT_GOAL_ML

        val schedule = buildSchedule(startTime, endTime, goalMl, alarmInterval)
        val nextAlarmSlot = runCatching {
            getNextAlarmSlot(now, startTime, endTime, goalMl, alarmInterval)
        }.getOrNull()

        HomeAlarmState.Active(
            startTime = startTime,
            endTime = endTime,
            interval = alarmInterval,
            schedule = schedule,
            nextAlarmSlot = nextAlarmSlot,
            isInActiveHours = computeIsInActiveHours(now, startTime, endTime),
            now = now,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeAlarmState.Loading)

    private fun buildSchedule(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        goalMl: Float,
        interval: AlarmInterval,
    ): DrinkSchedule? = runCatching {
        calculator.scheduleByInterval(
            startTime = TimeResult(startTime.hour, startTime.minute, 0, 0L),
            endTime = TimeResult(endTime.hour, endTime.minute, 0, 0L),
            goalAmountMl = goalMl,
            desiredIntervalMinutes = (interval.hour * 60 + interval.minute).coerceAtLeast(1),
        )
    }.getOrNull()

    private fun computeIsInActiveHours(
        now: LocalDateTime,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        val nowMin = now.hour * 60 + now.minute
        val startMin = startTime.hour * 60 + startTime.minute
        val endMin = endTime.hour * 60 + endTime.minute
        return if (startMin <= endMin) nowMin in startMin..endMin
        else nowMin >= startMin || nowMin <= endMin
    }
}
