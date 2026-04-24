package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.ui.inputfield.goalAmountToString
import com.eahm.drinkwaterapp.ui.inputfield.validateAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    private val records = historyDate.map {
        withContext(Dispatchers.IO) {
            dao.findByTimestamp(
                start = it.toStartOfDayTime,
                end = it.toEndOfDayTime,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    private val firstSetup = prefsStorage.getFirstSetupDone()

    val state = combine(
        historyDate,
        records,
        firstSetup,
        goalAmount,
        drinkAlarmManager.state,
    ) { date, records, _, goalAmount, drinkAlarmState ->
        DrinkState.Data(
            date = date,
            records = records
                .sortedBy { it.timestamp }
                .reversed(),
            goalAmount = goalAmount.goalAmountToString(),
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
