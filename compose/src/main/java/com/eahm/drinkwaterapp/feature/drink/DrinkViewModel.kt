package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.GoalReachedWidget
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class DrinkViewModel(
    val application: Application,
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    @OptIn(ExperimentalTime::class)
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val historyDate = MutableStateFlow(today)

    private val goalAmount = prefsStorage.getGoalAmount()

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
        historyDate,
        records,
        drinkStats,
        drinkAlarmManager.state,
    ) { date, records, drinkStats, drinkAlarmState ->
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

        DrinkState.Data(
            date = date,
            historyRecords = historyRecords,
            drinkStats = drinkStats,
            isAlarmActive = drinkAlarmState.active,
            alarmInterval = drinkAlarmState.interval,
            alarmNextInterval = drinkAlarmState.nextInterval,
        )
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

                drinkAlarmManager.setupAlarm(application)
            }

            if (Settings.canDrawOverlays(application)) {
                val intent = Intent(application, GoalReachedWidget::class.java)
                application.startService(intent)
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

    fun onStopAlarm() = drinkAlarmManager.stop()
    fun getRemainingAlarmTime() = drinkAlarmManager.remainingTime()
}
