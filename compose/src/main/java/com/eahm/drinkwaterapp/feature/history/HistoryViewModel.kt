package com.eahm.drinkwaterapp.feature.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.backup.data.SyncCoordinator
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.feature.drink.DateTimeZoneChangedObserver
import com.eahm.drinkwaterapp.ui.historylist.DayGroup
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

internal class HistoryViewModel(
    val application: Application,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
    private val syncCoordinator: SyncCoordinator,
    private val backupPreferences: BackupPreferences,
) : ViewModel() {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    // Tracks the real current date/time; updates at midnight or timezone change.
    private val currentNow = MutableStateFlow(dateTimeZoneChangedObserver.currentDateTime.value)

    // The date the user is currently viewing. Starts at today; auto-advances only when
    // the user is already on "today" so browsing past dates is unaffected.
    private val historyDate = MutableStateFlow(dateTimeZoneChangedObserver.currentDateTime.value)

    init {
        viewModelScope.launch {
            dateTimeZoneChangedObserver.currentDateTime.collect { newNow ->
                val oldDate = currentNow.value.date
                val newDate = newNow.date
                if (newDate != oldDate) {
                    // Only snap the viewed date forward if the user was already on "today".
                    if (historyDate.value.date == oldDate) {
                        historyDate.value = newNow
                    }
                    currentNow.value = newNow
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val records = historyDate
        .flatMapLatest {
            historyDao.findByTimestamp(
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val consumedMl = historyDate
        .flatMapLatest { date ->
            historyDao.getTotalDrinkAmount(
                start = date.toStartOfDayTime,
                end = date.toEndOfDayTime,
            )
        }
        .flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val goalMl = historyDate
        .flatMapLatest { date ->
            goalDao.getGoalForDate(date.date.toEpochDays())
        }
        .map { it?.goalMl ?: DEFAULT_GOAL_ML }
        .flowOn(Dispatchers.IO)

    val state = combine(
        historyDate,
        records,
        goalMl,
        consumedMl,
        currentNow,
    ) { date, records, goal, consumed, now ->
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
                    isCurrentHour = dayGroup.hour == now.hour &&
                            dayGroup.day == now.day &&
                            dayGroup.month == now.month.ordinal &&
                            dayGroup.year == now.year,
                    items = items,
                )
            }
            .sortedByDescending { it.hour }
            .toImmutableList()

        val isToday = date.date == now.date

        HistoryState.Data(
            date = date,
            historyRecords = historyRecords,
            goalMl = goal,
            consumedMl = consumed,
            isToday = isToday,
        )
    }.combine(prefsStorage.getTimeFormatPreference()) { historyState, timeFormat ->
        historyState.copy(timeFormatPreference = timeFormat)
    }.combine(prefsStorage.getUnitPreference()) { historyState, unitPref ->
        historyState.copy(unitPreference = unitPref)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = HistoryState.Loading,
    )

    fun onDelete(it: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (backupPreferences.getBackupEnabled()) {
                    // Backup on: soft delete so the removal syncs as a tombstone.
                    val now = System.currentTimeMillis()
                    historyDao.softDelete(id = it.id, deletedAt = now, updatedAt = now)
                } else {
                    // Backup off: nothing to reconcile — delete for real, no tombstone.
                    historyDao.delete(it)
                }
            }
            syncCoordinator.onDataChanged()
        }
    }

    fun onUpdateRecord(entity: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                historyDao.update(entity)
            }
        }
    }

    fun onDateChanged(date: LocalDateTime) {
        historyDate.update { date }
    }

    fun goToToday() {
        historyDate.value = currentNow.value
    }
}
