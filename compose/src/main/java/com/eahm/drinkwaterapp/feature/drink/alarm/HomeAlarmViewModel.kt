package com.eahm.drinkwaterapp.feature.drink.alarm

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
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
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime

internal class HomeAlarmViewModel(
    private val application: Application,
    clockTickObserver: ClockTickObserver,
    private val notificationPermissionObserver: NotificationPermissionObserver,
    private val prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) : ViewModel() {

    private val calculator = DrinkAlarmCalculator2()

    // Increments each time the app comes to the foreground, forcing the state
    // to recompute alarm status (exact vs inexact) without waiting for the next minute tick.
    private val refreshTrigger = MutableStateFlow(0L)
    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
    }

    private val alarmPrefs = combine(
        prefsStorage.getInterval(),
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { interval, start, end -> Triple(interval, start, end) }

    val state = combine(
        notificationPermissionObserver.hasPermission,
        clockTickObserver.observe(ClockGranularity.MINUTE),
        alarmPrefs,
        refreshTrigger,
    ) { hasPermission, now, prefs, _ ->
        if (!hasPermission) return@combine HomeAlarmState.NoPermission

        val (intervalPref, startTimePref, endTimePref) = prefs
        val startTime = startTimePref.toLocalDateTime()
        val endTime = endTimePref.toLocalDateTime()
        val alarmInterval = AlarmInterval(intervalPref.hour, intervalPref.minute)

        val isExactActive =
            AlarmSchedulerV3.isExactAlarmActive(application, DrinkAlarmManagerV2.EXACT_ALARM_ID)
        val isInexactActive =
            AlarmSchedulerV3.isInexactAlarmActive(application, DrinkAlarmManagerV2.INEXACT_ALARM_ID)
        if (!isExactActive && !isInexactActive) return@combine HomeAlarmState.NotConfigured

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
            isInexact = isInexactActive && !isExactActive,
        )
    }.combine(prefsStorage.getTimeFormatPreference()) { alarmState, timeFormat ->
        if (alarmState is HomeAlarmState.Active) alarmState.copy(timeFormatPreference = timeFormat)
        else alarmState
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
