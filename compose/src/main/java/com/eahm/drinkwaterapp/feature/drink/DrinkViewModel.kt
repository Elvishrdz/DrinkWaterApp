package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
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

    // Emits the current LocalDateTime and re-emits whenever the calendar date changes
    // (midnight rollover, manual date/time edit, or timezone switch).
    // distinctUntilChangedBy ensures only a real day boundary triggers downstream work.
    @OptIn(ExperimentalTime::class)
    private val currentDay: StateFlow<LocalDateTime> = callbackFlow {
        trySend(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))

        val receiver = object : BroadcastReceiver() {
            @OptIn(ExperimentalTime::class)
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
            }
        }

        application.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_DATE_CHANGED)     // midnight rollover
                addAction(Intent.ACTION_TIME_CHANGED)     // manual time edit
                addAction(Intent.ACTION_TIMEZONE_CHANGED) // timezone switch
            },
        )

        awaitClose { application.unregisterReceiver(receiver) }
    }
        .distinctUntilChangedBy { it.date } // only propagate when the date itself changes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        )

    private val historyDate = MutableStateFlow(currentDay.value)

    init {
        // Keep historyDate in sync with the current calendar day.
        // The distinctUntilChangedBy above ensures this only fires on real date changes.
        viewModelScope.launch {
            currentDay.collect { historyDate.value = it }
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

    private val history = combine(
        historyDate,
        records,
    ) { date, records ->
        val todayLogs = records
            .filter { it.timestamp in date.toStartOfDayTime..date.toEndOfDayTime }
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
            todayLogs = todayLogs
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val drinkStats = combine(
        prefsStorage.getGoalAmount(),
        currentDay.flatMapLatest { day ->
            dao.getTotalDrinkAmount(start = day.toStartOfDayTime, end = day.toEndOfDayTime)
        },
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
        drinkStats,
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

                // TODO: check if we reached the goal now
                confettiController.showConfetti()

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
    }
}
