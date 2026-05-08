package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoService
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.ClockGranularity
import com.eahm.drinkwaterapp.feature.drink.ClockTickObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateTime

internal class AlarmIntervalsViewModel(
    clockTickObserver: ClockTickObserver,
    private val whatToDoService: WhatToDoService,
    prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) : ViewModel() {

    private val _events = Channel<AlarmIntervalsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmIntervalsEvent> = _events.receiveAsFlow()

    val state = combine(
        clockTickObserver.observe(ClockGranularity.SECOND),
        prefsStorage.getInterval(),
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { time, interval, startTime, endTime ->
        val todayEpoch = time.date.toEpochDays()

        val goalMl = database.getDailyGoalDao()
            .getGoalForDate(todayEpoch)
            .firstOrNull()
            ?.goalMl ?: DEFAULT_GOAL_ML

        buildDataState(
            now = time,
            startTime = startTime.toLocalDateTime(),
            endTime = endTime.toLocalDateTime(),
            interval = AlarmInterval(interval.hour, interval.minute),
            goalMl = goalMl,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        AlarmIntervalsState.Loading,
    )

    fun onAction(action: AlarmIntervalsAction) {
        // handled as the feature grows
    }

    private fun buildDataState(
        now: LocalDateTime,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        interval: AlarmInterval,
        goalMl: Float,
    ): AlarmIntervalsState.Data {
        val whatToDoResult = whatToDoService.whatShouldIDo(
            localDateTime = now,
            startTime = startTime,
            endTime = endTime,
            interval = interval,
            goalMl = goalMl,
        )

        return AlarmIntervalsState.Data(
            now = now,
            startTime = startTime,
            endTime = endTime,
            goalMl = goalMl,
            interval = interval,
            intervalMinutes = interval.minute,
            whatToDoResult = whatToDoResult,
        )
    }
}
