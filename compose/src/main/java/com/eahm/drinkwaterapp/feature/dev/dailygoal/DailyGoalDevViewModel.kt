package com.eahm.drinkwaterapp.feature.dev.dailygoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
internal class DailyGoalDevViewModel(
    database: AppDatabase,
) : ViewModel() {

    private val dao = database.getDailyGoalDao()

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private val _selectedDate = MutableStateFlow(today)
    private val _inputGoalMl = MutableStateFlow(DEFAULT_GOAL_ML)

    private val todayGoal = dao.getGoalForDate(today.toEpochDays())
        .map { it?.goalMl ?: DEFAULT_GOAL_ML }

    val state = combine(
        todayGoal,
        dao.getAllGoals(),
        _selectedDate,
        _inputGoalMl,
    ) { todayGoalMl, allRecords, selectedDate, inputGoalMl ->
        DailyGoalDevState.Data(
            todayGoalMl = todayGoalMl,
            allRecords = allRecords,
            selectedDate = selectedDate,
            inputGoalMl = inputGoalMl,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DailyGoalDevState.Loading,
    )

    fun onSelectedDateChanged(date: LocalDate) {
        _selectedDate.update { date }
    }

    fun onInputGoalChanged(goalMl: Float) {
        _inputGoalMl.update { goalMl }
    }

    fun setGoalFromToday() {
        viewModelScope.launch {
            val goalMl = _inputGoalMl.value
            val epochDay = today.toEpochDays()
            val existing = dao.getGoalOnDate(epochDay).first()
            if (existing != null) dao.update(existing.copy(goalMl = goalMl))
            else dao.insert(DailyGoalEntity(effectiveDate = today, goalMl = goalMl))
        }
    }

    fun setGoalForSelectedDay() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val newGoalMl = _inputGoalMl.value
            val epochDay = date.toEpochDays()

            val previousGoal = dao.getGoalForDate(epochDay).first()?.goalMl ?: DEFAULT_GOAL_ML

            val existing = dao.getGoalOnDate(epochDay).first()
            if (existing != null) dao.update(existing.copy(goalMl = newGoalMl))
            else dao.insert(DailyGoalEntity(effectiveDate = date, goalMl = newGoalMl))

            val nextDay = date.plus(1, DateTimeUnit.DAY)
            val nextRecord = dao.getFirstGoalAfter(epochDay).first()
            if (nextRecord == null || nextRecord.effectiveDate != nextDay) {
                dao.insert(DailyGoalEntity(effectiveDate = nextDay, goalMl = previousGoal))
            }
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
