package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.goalreachedwidget.GoalReachedWidget
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
    val dateTimeObserver: DateTimeObserver,
    val drinkStatsObserver: DrinkStatsObserver,
) : ViewModel() {

    private val dao = database.getHistoryDao()

    private val historyDate = MutableStateFlow(dateTimeObserver.currentDay.value)

    init {
        viewModelScope.launch {
            dateTimeObserver.currentDay.collect { historyDate.value = it }
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
        prefsStorage.getUsername(),
        prefsStorage.getDrinkPresets(),
    ) { username, drinkPresets ->
        val allDrinkPresets = drinkPresets
            .sortedBy { it.order }
            .toList()

        DrinkExtras(
            username = username,
            quickDrinkPresets = allDrinkPresets.take(QUICK_DRINK_PRESETS_AMOUNT),
            allDrinkPresets = allDrinkPresets,
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
    ) { extras, history, drinkStats ->
        if (history == null || extras == null) {
            DrinkState.Loading
        } else {
            DrinkState.Data(
                username = extras.username,
                currentStreak = 0,
                quickDrinkPresets = extras.quickDrinkPresets,
                allDrinkPresets = extras.allDrinkPresets,
                todayLogs = history.todayLogs,
                drinkStats = drinkStats,
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

                drinkAlarmManager.setupAlarm(application)
            }
        }
    }

    fun onCreatePreset(amount: Float) {
        viewModelScope.launch {
            prefsStorage.getDrinkPresets().firstOrNull()?.let { currentPresets ->
                val isUnique = !currentPresets.any { it.amountMl.equals(amount) }

                if (isUnique) {
                    val new = DrinkPreset(
                        order = currentPresets.size.plus(1),
                        amountMl = amount,
                    )

                    val updated = currentPresets
                        .toMutableSet()
                        .apply { add(new) }
                        .sortedBy { it.order }
                        .toSet()

                    prefsStorage.setDrinkPresets(updated)
                }
            }
        }
    }

    fun onDeletePreset(preset: DrinkPreset) {
        viewModelScope.launch {
            prefsStorage.getDrinkPresets().firstOrNull()?.let { currentPresets ->
                val updated = currentPresets
                    .filter { it.amountMl != preset.amountMl }
                    .toSet()
                prefsStorage.setDrinkPresets(updated)
            }
        }
    }

    fun onRestorePreset(preset: DrinkPreset) {
        viewModelScope.launch {
            prefsStorage.getDrinkPresets().firstOrNull()?.let { currentPresets ->
                if (currentPresets.none { it.amountMl == preset.amountMl }) {
                    val updated = currentPresets.toMutableSet().apply { add(preset) }
                    prefsStorage.setDrinkPresets(updated)
                }
            }
        }
    }

    fun onPresetsReordered(newOrder: List<DrinkPreset>, reorderingQuickPresets: Boolean) {
        viewModelScope.launch {
            val reordered = newOrder.mapIndexed { index, preset ->
                preset.copy(order = index + 1)
            }.toMutableSet()

            if (reorderingQuickPresets) {
                val currentPresets = prefsStorage.getDrinkPresets().firstOrNull()
                    ?.toMutableSet()

                val remainingPresets = currentPresets
                    ?.filterIndexed { index, _ -> index >= QUICK_DRINK_PRESETS_AMOUNT }

                remainingPresets?.let {
                    reordered.addAll(it)
                }
            }

            prefsStorage.setDrinkPresets(reordered)
        }
    }

    fun displayGoalReachedOverlay() {
        if (Settings.canDrawOverlays(application)) {
            val intent = Intent(application, GoalReachedWidget::class.java)
            application.startService(intent)
        }
    }

    companion object {

        const val QUICK_DRINK_PRESETS_AMOUNT = 3
        const val TODAY_LOGS_AMOUNT = 4
    }
}
