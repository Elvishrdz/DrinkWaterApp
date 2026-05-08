package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmCalculator2
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoService
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.getNextAlarmSlot
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.domain.extension.toLocalDateTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.ScheduleMode
import com.eahm.drinkwaterapp.feature.drink.ClockGranularity
import com.eahm.drinkwaterapp.feature.drink.ClockTickObserver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class AlarmIntervalsViewModel(
    clockTickObserver: ClockTickObserver,
    private val whatToDoService: WhatToDoService,
    prefsStorage: PrefsStorage,
    private val database: AppDatabase,
) : ViewModel() {

    private val _events = Channel<AlarmIntervalsEvent>(Channel.BUFFERED)
    val events: Flow<AlarmIntervalsEvent> = _events.receiveAsFlow()

    @OptIn(ExperimentalTime::class)
    private val initialNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val time = MutableStateFlow(initialNow)
    private val customTime = MutableStateFlow<LocalDateTime?>(null)

    private val calculator = DrinkAlarmCalculator2()

    private val dateTimes = combine(
        time,
        clockTickObserver.observe(ClockGranularity.SECOND),
        customTime,
    ) { time, now, customTime ->
        val selectedTime = customTime ?: now
        Triple(selectedTime, now, customTime)

    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Triple(initialNow, initialNow, null),
    )

    val state = combine(
        dateTimes,
        prefsStorage.getInterval(),
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { dateTimes, interval, startTime, endTime ->

        val timeEpoch = dateTimes.first.date.toEpochDays()

        val goalMl = database.getDailyGoalDao()
            .getGoalForDate(timeEpoch)
            .firstOrNull()
            ?.goalMl ?: DEFAULT_GOAL_ML

        buildDataState(
            time = dateTimes.first,
            now = dateTimes.second,
            customTime = dateTimes.third,
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
        when (action) {
            is AlarmIntervalsAction.SetCustomTime -> customTime.update { action.dateTime }
            AlarmIntervalsAction.ClearCustomTime -> customTime.update { null }
        }
    }

    private fun buildDataState(
        time: LocalDateTime,
        now: LocalDateTime,
        customTime: LocalDateTime?,
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

        val schedule = buildSchedule(
            startTime = startTime,
            endTime = endTime,
            goalMl = goalMl,
            interval = interval,
        )

        val nextAlarmSlot = getNextAlarmSlot(
            time = time,
            startTime = startTime,
            endTime = endTime,
            goalMl = goalMl,
            interval = interval,
        )

        return AlarmIntervalsState.Data(
            time = time,
            now = now,
            customTime = customTime,
            startTime = startTime,
            endTime = endTime,
            goalMl = goalMl,
            interval = interval,
            intervalMinutes = interval.minute,
            whatToDoResult = whatToDoResult,
            schedule = schedule,
            nextAlarmSlot = nextAlarmSlot,
        )
    }

    private fun buildSchedule(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        goalMl: Float,
        interval: AlarmInterval,
        mode: ScheduleMode = ScheduleMode.BY_INTERVAL,
    ): DrinkSchedule? {
        val start = DayTime(startTime.hour, startTime.minute)
        val end = DayTime(endTime.hour, endTime.minute)

        return runCatching {
            when (mode) {
                ScheduleMode.BY_AMOUNT -> calculator.scheduleByAmount(
                    startTime = start,
                    endTime = end,
                    goalAmountMl = goalMl,
                    amountPerDrinkMl = 200f,
                )

                ScheduleMode.BY_INTERVAL -> calculator.scheduleByInterval(
                    startTime = start,
                    endTime = end,
                    goalAmountMl = goalMl,
                    desiredIntervalMinutes = interval.minute, // TODO: check whole minutes of interval
                )
            }
        }.getOrNull()
    }

    private fun getNextAlarmSlot(
        time: LocalDateTime,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        goalMl: Float,
        interval: AlarmInterval,
        mode: ScheduleMode = ScheduleMode.BY_INTERVAL,
    ): LocalDateTime? {
        return if (mode == ScheduleMode.BY_INTERVAL) {
            runCatching {
                getNextAlarmSlot(
                    now = time,
                    startTime = startTime,
                    endTime = endTime,
                    goalMl = goalMl,
                    interval = interval,
                )

            }.getOrNull()
        } else {
            null
        }
    }
}
