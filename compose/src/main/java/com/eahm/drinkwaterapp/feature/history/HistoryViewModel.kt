package com.eahm.drinkwaterapp.feature.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.ui.historylist.DayGroup
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
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

internal class HistoryViewModel(
    val application: Application,
    val database: AppDatabase,
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

    val state = combine(
        historyDate,
        records
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

        HistoryState.Data(
            date = date,
            historyRecords = historyRecords,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = HistoryState.Loading,
    )

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
}
