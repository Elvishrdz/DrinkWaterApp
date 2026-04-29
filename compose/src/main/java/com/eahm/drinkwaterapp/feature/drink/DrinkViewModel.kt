package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.GoalReachedWidget
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.data.HistoryData
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.ui.historylist.DayGroup
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import com.eahm.drinkwaterapp.ui.inputfield.validateAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class DrinkViewModel(
    val application: Application,
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val confettiController: ConfettiController,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    @OptIn(ExperimentalTime::class)
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val historyDate = MutableStateFlow(today)

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
            //  TODO: filter from current time to latest 4 records
            .filter { it.timestamp in today.toStartOfDayTime..today.toEndOfDayTime }
            //.take(4)
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
        prefsStorage.getGoalAmount(),
        dao.getTotalDrinkAmount(start = today.toStartOfDayTime, end = today.toEndOfDayTime),
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

    private val extras = combine(
        prefsStorage.getUsername(),
        prefsStorage.getDrinkPresets(),
    ) { username, drinkPresets ->
        DrinkExtras(
            username = username,
            drinkPresets = drinkPresets
                .sortedBy { it.order }
                .toList(),
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    val state = combine(
        extras,
        history,
        drinkStats,
        drinkAlarmManager.state,
    ) { extras, history, drinkStats, drinkAlarmState ->
        if (history == null || extras == null) {
            DrinkState.Loading
        } else {
            DrinkState.Data(
                username = extras.username,
                currentStreak = 0,
                date = history.date,
                drinkPresets = extras.drinkPresets,
                historyRecords = history.historyRecords,
                todayLogs = history.todayLogs,
                drinkStats = drinkStats,
                isAlarmActive = drinkAlarmState.active,
                alarmInterval = drinkAlarmState.interval,
                alarmNextInterval = drinkAlarmState.nextInterval,
            )
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DrinkState.Loading,
    )

    fun onDrink(amount: Float) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val record = HistoryEntity(
                    amount = amount,
                    status = Status.IN_APP,
                )

                dao.insertAll(record)

                // TODO: check if we reached the goal now
                confettiController.showConfetti()

                drinkAlarmManager.setupAlarm(application)
            }
        }
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
                    prefsStorage.setGoalAmount(amount)
                }
        }
    }

    fun onAlarmIntervalChange(alarmInterval: AlarmInterval) {
        drinkAlarmManager.setInterval(alarmInterval.hour, alarmInterval.minute)
    }

    fun displayGoalReachedOverlay() {
        if (Settings.canDrawOverlays(application)) {
            val intent = Intent(application, GoalReachedWidget::class.java)
            application.startService(intent)
        }
    }

    fun onStopAlarm() = drinkAlarmManager.stop()
    fun getRemainingAlarmTime() = drinkAlarmManager.remainingTime()
}
