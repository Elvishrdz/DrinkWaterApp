package com.eahm.drinkwaterapp.feature.dev.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.streak.StreakRepository
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class StreakDevViewModel(
    streakRepository: StreakRepository,
    private val prefsStorage: PrefsStorage,
    database: AppDatabase,
) : ViewModel() {

    private val historyDao = database.getHistoryDao()

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private val _selectedDate = MutableStateFlow(today)
    private val _drinkAmountInput = MutableStateFlow(200f)

    private val storedBestGoalStreak = prefsStorage.getBestGoalStreak()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val storedBestDrinkStreak = prefsStorage.getBestDrinkStreak()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val state = combine(
        streakRepository.streakData,
        storedBestGoalStreak,
        storedBestDrinkStreak,
        _selectedDate,
        _drinkAmountInput,
    ) { streakData, storedGoal, storedDrink, date, amount ->
        StreakDevState.Data(
            currentGoalStreak = streakData.currentGoalStreak,
            currentDrinkStreak = streakData.currentDrinkStreak,
            bestGoalStreak = streakData.bestGoalStreak,
            bestDrinkStreak = streakData.bestDrinkStreak,
            personalBestMl = streakData.personalBestMl,
            storedBestGoalStreak = storedGoal,
            storedBestDrinkStreak = storedDrink,
            selectedDate = date,
            drinkAmountInput = amount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = StreakDevState.Loading,
    )

    fun onSelectedDateChanged(date: LocalDate) {
        _selectedDate.update { date }
    }

    fun onDrinkAmountChanged(amount: Float) {
        _drinkAmountInput.update { amount }
    }

    fun addDrinkForSelectedDate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val date = _selectedDate.value
                val timestamp = LocalDateTime(date.year, date.month, date.day, 12, 0, 0)
                historyDao.insertAll(
                    HistoryEntity(
                        amount = _drinkAmountInput.value,
                        status = Status.IN_APP,
                        timestamp = timestamp,
                    )
                )
            }
        }
    }

    fun clearHistoryForSelectedDate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val date = _selectedDate.value
                val start = LocalDateTime(date.year, date.month, date.day, 0, 0, 0)
                val end = LocalDateTime(date.year, date.month, date.day, 23, 59, 59, 999_999_999)
                val records = historyDao.findByTimestamp(start, end).first()
                records.forEach { historyDao.delete(it) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val all = historyDao.getAll().first()
                all.forEach { historyDao.delete(it) }
            }
        }
    }

    fun setStoredBestGoalStreak(value: Int) {
        viewModelScope.launch { prefsStorage.setBestGoalStreak(value) }
    }

    fun setStoredBestDrinkStreak(value: Int) {
        viewModelScope.launch { prefsStorage.setBestDrinkStreak(value) }
    }

    fun clearStoredBests() {
        viewModelScope.launch {
            prefsStorage.setBestGoalStreak(0)
            prefsStorage.setBestDrinkStreak(0)
        }
    }
}
