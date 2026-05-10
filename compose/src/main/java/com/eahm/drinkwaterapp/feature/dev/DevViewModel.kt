package com.eahm.drinkwaterapp.feature.dev

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.ui.historylist.DayGroup
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.inputfield.validateAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class DevViewModel(
    val application: Application,
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val confettiController: ConfettiController,
) : ViewModel() {

    private val dao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    @OptIn(ExperimentalTime::class)
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val todayDate: LocalDate = today.date

    private val historyDate = MutableStateFlow(today)

    private val goalAmount = goalDao.getGoalForDate(todayDate.toEpochDays())
        .map { it?.goalMl ?: DEFAULT_GOAL_ML }

    private val drinkAmount = dao.getTotalDrinkAmount(today.toStartOfDayTime, today.toEndOfDayTime)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val records = historyDate
        .flatMapLatest {
            dao.findByTimestamp(
                start = it.toStartOfDayTime,
                end = it.toEndOfDayTime,
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList(),
        )

    private data class HistoryData(
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
        val todayLogs: List<DrinkLogEntry>
    )

    @OptIn(ExperimentalTime::class)
    private val history = combine(
        historyDate,
        records,
    ) { date, records ->
        val historyRecords = records
            .sortedBy { it.timestamp }
            .reversed()
            .map { it.toUI() }
            .groupBy {
                DayGroup(
                    hour = it.timestamp.hour,
                    day = it.timestamp.day,
                    month = it.timestamp.month.ordinal,
                    year = it.timestamp.year,
                )
            }
            .toSortedMap(compareBy { it.hour })
            .mapNotNull { (dayGroup, items) ->
                HourGroup(
                    hour = dayGroup.hour,
                    isCurrentHour = dayGroup.hour == today.hour &&
                            dayGroup.day == today.day &&
                            dayGroup.month == today.month.ordinal &&
                            dayGroup.year == today.year,
                    items = items,
                )
            }
            .sortedByDescending { it.hour }

        val todayLogs = records
            .filter { it.timestamp in today.toStartOfDayTime..today.toEndOfDayTime }
            .sortedByDescending { it.timestamp }
            .map {
                DrinkLogEntry(
                    time = it.timestamp.time.format(
                        LocalTime.Format {
                            hour()
                            char(':')
                            minute()
                        }
                    ),
                    amount = it.amount,
                    timestamp = it.timestamp
                        .toInstant(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()
                        .toString(),
                )
            }

        HistoryData(
            date = date,
            historyRecords = historyRecords,
            todayLogs = todayLogs
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val drinkStats = combine(
        goalAmount,
        drinkAmount,
    ) { goalAmount, drinkAmount ->
        DrinkStats(
            goalAmount = goalAmount,
            drinkAmount = drinkAmount,
            goalReached = drinkAmount >= goalAmount,
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    val state = combine(
        history,
        drinkStats,
        drinkAlarmManager.state,
        confettiController.state,
    ) { history, drinkStats, drinkAlarmState, confettiActive ->

        if (history == null) {
            DevState.Loading
        } else {
            DevState.Data(
                username = "Elvis",
                currentStreak = 0,
                date = history.date,
                historyRecords = history.historyRecords,
                todayLogs = history.todayLogs,
                drinkStats = drinkStats,
                isAlarmActive = drinkAlarmState.active,
                alarmInterval = drinkAlarmState.interval,
                confettiButtonEnabled = !confettiActive,
            )
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DevState.Loading,
    )

    fun onDrink(amount: Float) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val record = HistoryEntity(
                    amount = amount,
                    status = Status.IN_APP,
                )

                dao.insertAll(record)

                drinkAlarmManager.setupAlarm(AlarmSource.DrinkButtonDevMenu)
            }
        }
    }

    fun showConfetti() {
        confettiController.showConfetti()
    }

    fun onDelete(it: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.delete(it)
            }
        }
    }

    fun onDateChanged(date: LocalDateTime) {
        historyDate.update { date }
    }

    fun onGoalChanged(goal: String) {
        viewModelScope.launch {
            goal.toFloatOrNull()
                ?.takeIf { validateAmount(it).isValid }
                ?.let { amount ->
                    val epochDay = todayDate.toEpochDays()
                    val existing = goalDao.getGoalOnDate(epochDay).first()
                    if (existing != null) goalDao.update(existing.copy(goalMl = amount))
                    else goalDao.insert(DailyGoalEntity(effectiveDate = todayDate, goalMl = amount))
                }
        }
    }

    fun onAlarmIntervalChange(alarmInterval: AlarmInterval) {
        viewModelScope.launch {
            drinkAlarmManager.setInterval(alarmInterval.hour, alarmInterval.minute)
        }
    }

    fun onStopAlarm() = drinkAlarmManager.stopAlarm()
}
