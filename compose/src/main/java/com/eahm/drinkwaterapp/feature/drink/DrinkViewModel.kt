package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.CelebrationStyle
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.GoalReachedWidget
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.createIntent
import com.eahm.drinkwaterapp.core.data.streak.StreakRepository
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.feature.drink.data.HistoryData
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

internal class DrinkViewModel(
    val application: Application,
    val drinkAlarmManager: DrinkAlarmManager,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val confettiController: ConfettiController,
    val dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
    val drinkStatsObserver: DrinkStatsObserver,
    val streakRepository: StreakRepository,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    private val historyDate = MutableStateFlow(dateTimeZoneChangedObserver.currentDateTime.value)

    init {
        viewModelScope.launch {
            dateTimeZoneChangedObserver.currentDateTime.collect { historyDate.value = it }
        }

        viewModelScope.launch {
            drinkStatsObserver.goalReachedEvent.collect {
                confettiController.showConfetti()
                displayGoalReachedOverlay()
            }
        }
    }

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

    @OptIn(ExperimentalTime::class)
    private val history = combine(
        historyDate,
        records,
    ) { date, records ->
        val todayLogs = records
            .filter { it.timestamp in date.toStartOfDayTime..date.toEndOfDayTime }
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
            .take(TODAY_LOGS_AMOUNT)

        HistoryData(
            date = date,
            todayLogs = todayLogs,
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val extras = combine(
        streakRepository.streakData,
        prefsStorage.getDrinkSuggestions(),
    ) { streakData, drinkPresets ->
        val allDrinkPresets = drinkPresets
            .sortedBy { it.order }
            .toList()

        DrinkExtras(
            streakData = streakData,
            quickDrinkSuggestions = allDrinkPresets.take(QUICK_DRINK_PRESETS_AMOUNT),
            allDrinkSuggestions = allDrinkPresets,
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    val state = combine(
        extras,
        history,
        drinkStatsObserver.drinkStats,
        drinkAlarmManager.state
    ) { extras, history, drinkStats, alarmState ->
        if (history == null || extras == null) {
            DrinkState.Loading
        } else {
            DrinkState.Data(
                drinkStreakDays = extras.streakData.drinkStreakDays,
                showStreakBadge = extras.streakData.drinkStreakDays > 0,
                quickDrinkSuggestions = extras.quickDrinkSuggestions,
                allDrinkSuggestions = extras.allDrinkSuggestions,
                todayLogs = history.todayLogs,
                drinkStats = drinkStats,
                alarmTriggerAtMs = alarmState.alarmStorageInfo?.alarmTriggerAtMillis,
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

                drinkAlarmManager.setupAlarm(AlarmSource.DrinkButton)
            }
        }
    }

    fun onCreatePreset(amount: Float) {
        viewModelScope.launch {
            prefsStorage.getDrinkSuggestions().firstOrNull()?.let { currentPresets ->
                val isUnique = !currentPresets.any { it.amountMl.equals(amount) }

                if (isUnique) {
                    val new = DrinkSuggestion(
                        order = currentPresets.size.plus(1),
                        amountMl = amount,
                    )

                    val updated = currentPresets
                        .toMutableSet()
                        .apply { add(new) }
                        .sortedBy { it.order }
                        .toSet()

                    prefsStorage.setDrinkSuggestions(updated)
                }
            }
        }
    }

    fun onDeletePreset(preset: DrinkSuggestion) {
        viewModelScope.launch {
            prefsStorage.getDrinkSuggestions().firstOrNull()?.let { currentPresets ->
                val updated = currentPresets
                    .filter { it.amountMl != preset.amountMl }
                    .toSet()
                prefsStorage.setDrinkSuggestions(updated)
            }
        }
    }

    fun onRestorePreset(preset: DrinkSuggestion) {
        viewModelScope.launch {
            prefsStorage.getDrinkSuggestions().firstOrNull()?.let { currentPresets ->
                if (currentPresets.none { it.amountMl == preset.amountMl }) {
                    val updated = currentPresets.toMutableSet().apply { add(preset) }
                    prefsStorage.setDrinkSuggestions(updated)
                }
            }
        }
    }

    fun onPresetsReordered(newOrder: List<DrinkSuggestion>, reorderingQuickPresets: Boolean) {
        viewModelScope.launch {
            val reordered = newOrder.mapIndexed { index, preset ->
                preset.copy(order = index + 1)
            }.toMutableSet()

            if (reorderingQuickPresets) {
                val currentPresets = prefsStorage.getDrinkSuggestions().firstOrNull()
                    ?.toMutableSet()

                val remainingPresets = currentPresets
                    ?.filterIndexed { index, _ -> index >= QUICK_DRINK_PRESETS_AMOUNT }

                remainingPresets?.let {
                    reordered.addAll(it)
                }
            }

            prefsStorage.setDrinkSuggestions(reordered)
        }
    }

    private suspend fun displayGoalReachedOverlay() {
        if (Settings.canDrawOverlays(application)) {
            val currentState = state.value as? DrinkState.Data
            val intent = GoalReachedWidget.createIntent(
                context = application,
                style = CelebrationStyle.Ocean,
                goalMl = currentState?.drinkStats?.goalAmount ?: 2400f,
                drinkCount = records.value.size.coerceAtLeast(1),
                streakDays = streakRepository.streakData.firstOrNull()?.goalStreakDays ?: 0,
            )
            application.startService(intent)
        }
    }

    companion object {

        const val QUICK_DRINK_PRESETS_AMOUNT = 3
        const val TODAY_LOGS_AMOUNT = 4
    }
}
