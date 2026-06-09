package com.eahm.drinkwaterapp.feature.history.v2

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.backup.data.SyncCoordinator
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.ui.historylist.DayGroup
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class HistoryV2ViewModel(
    val application: Application,
    val database: AppDatabase,
    private val syncCoordinator: SyncCoordinator,
    private val backupPreferences: BackupPreferences,
) : ViewModel() {

    private companion object {
        const val GOAL_ML = 2200
        const val PAD = 3  // disabled padding days on each side of the enabled range
    }

    private val dao = database.getHistoryDao()

    private val todayDateTime: LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val todayDate: LocalDate = todayDateTime.date

    // Currently selected day (ISO key "YYYY-MM-DD")
    private val selectedDayKey = MutableStateFlow(todayDate.toString())

    // Earliest date that has any records in the DB
    private val earliestDate = dao.getEarliestRecord()
        .map { it?.timestamp?.date }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Records for the currently selected day
    @OptIn(ExperimentalCoroutinesApi::class)
    private val selectedRecords = selectedDayKey
        .flatMapLatest { key ->
            val d = LocalDate.parse(key)
            dao.findByTimestamp(
                start = LocalDateTime(d.year, d.month, d.dayOfMonth, 0, 0, 0, 0),
                end = LocalDateTime(d.year, d.month, d.dayOfMonth, 23, 59, 59, 999_999_999),
            )
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val state = combine(
        earliestDate,
        selectedDayKey,
        selectedRecords,
    ) { earliest, key, records ->
        val firstEnabled = earliest ?: todayDate
        val days = buildDayList(firstEnabled)

        val totalMl = records.sumOf { it.amount.toInt() }
        val percent = minOf(100, ((totalMl.toDouble() / GOAL_ML) * 100).toInt())

        val hourGroups = records
            .sortedBy { it.timestamp }
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
            .map { (group, items) ->
                HourGroup(
                    hour = group.hour,
                    isCurrentHour = group.hour == todayDateTime.hour &&
                            group.day == todayDateTime.day &&
                            group.month == todayDateTime.month.ordinal &&
                            group.year == todayDateTime.year,
                    items = items,
                )
            }
            .sortedBy { it.hour }

        val enabledDays = days.filter { it.isEnabled }
        val selectedDay = days.find { it.key == key }
        val canGoPrev = key != enabledDays.firstOrNull()?.key
        val canGoNext = key != enabledDays.lastOrNull()?.key

        HistoryV2State.Data(
            days = days,
            selectedDayKey = key,
            selectedDayLabel = selectedDay?.fullLabel ?: "",
            totalMl = totalMl,
            drinkCount = records.size,
            goalMl = GOAL_ML,
            percent = percent,
            records = hourGroups,
            canGoPrev = canGoPrev,
            canGoNext = canGoNext,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, HistoryV2State.Loading)

    fun onSelectDay(key: String) = selectedDayKey.update { key }

    fun onPrevDay() {
        val data = state.value as? HistoryV2State.Data ?: return
        val enabled = data.days.filter { it.isEnabled }
        val idx = enabled.indexOfFirst { it.key == data.selectedDayKey }
        if (idx > 0) selectedDayKey.update { enabled[idx - 1].key }
    }

    fun onNextDay() {
        val data = state.value as? HistoryV2State.Data ?: return
        val enabled = data.days.filter { it.isEnabled }
        val idx = enabled.indexOfFirst { it.key == data.selectedDayKey }
        if (idx < enabled.lastIndex) selectedDayKey.update { enabled[idx + 1].key }
    }

    fun onDelete(entity: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (backupPreferences.getBackupEnabled()) {
                    // Backup on: soft delete so the removal syncs as a tombstone.
                    val now = System.currentTimeMillis()
                    dao.softDelete(id = entity.id, deletedAt = now, updatedAt = now)
                } else {
                    // Backup off: nothing to reconcile — delete for real, no tombstone.
                    dao.delete(entity)
                }
            }
            syncCoordinator.onDataChanged()
        }
    }

    fun onUpdateRecord(entity: HistoryEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.update(entity) }
        }
    }

    // ── Day strip builder ────────────────────────────────────────────────────

    private fun buildDayList(firstEnabled: LocalDate): List<DayItem> {
        val firstShown = firstEnabled - DatePeriod(days = PAD)
        val lastShown = todayDate + DatePeriod(days = PAD)
        val days = mutableListOf<DayItem>()
        var current = firstShown
        while (current <= lastShown) {
            days.add(
                DayItem(
                    key = current.toString(),
                    weekday = current.dayOfWeek.name.take(3)
                        .replaceFirstChar { it.uppercase() },
                    dayNum = current.dayOfMonth.toString(),
                    fullLabel = buildFullLabel(current),
                    isToday = current == todayDate,
                    isEnabled = current in firstEnabled..todayDate,
                    goalMet = false, // TODO: query per-day totals for goal-met dots
                )
            )
            current += DatePeriod(days = 1)
        }
        return days
    }

    private fun buildFullLabel(date: LocalDate): String {
        val mon = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val d = date.dayOfMonth
        return when (date) {
            todayDate -> "Today, $mon $d"
            todayDate - DatePeriod(days = 1) -> "Yesterday, $mon $d"
            else -> {
                val wk = date.dayOfWeek.name.take(3).replaceFirstChar { it.uppercase() }
                "$wk, $mon $d"
            }
        }
    }
}
