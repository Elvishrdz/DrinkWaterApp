package com.eahm.drinkwaterapp.feature.drink

import android.app.Application
import android.provider.Settings
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStatsObserver
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.data.streak.StreakRepository
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.database.entity.Status
import com.eahm.drinkwaterapp.core.domain.extension.toEndOfDayTime
import com.eahm.drinkwaterapp.core.domain.extension.toStartOfDayTime
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.drink.data.HistoryData
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.system.alarm.AlarmSchedulerV3
import com.eahm.drinkwaterapp.system.service.goalcelebration.GoalCelebrationOverlay
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.extension.createIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

internal class DrinkViewModel(
    val application: Application,
    val drinkAlarmManagerV2: DrinkAlarmManagerV2,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val confettiController: ConfettiController,
    val dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
    val drinkStatsObserver: DrinkStatsObserver,
    val streakRepository: StreakRepository,
    val trackingService: TrackingService,
    val activeHoursObserver: ActiveHoursObserver,
    notificationPermissionObserver: NotificationPermissionObserver,
    private val purchaseManager: PurchaseManager,
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
        prefsStorage.getTimeFormatPreference(),
    ) { date, records, timeFormatPref ->
        val is24Hour = when (timeFormatPref) {
            TimeFormatPreference.SYSTEM -> DateFormat.is24HourFormat(application)
            TimeFormatPreference.HOUR_12 -> false
            TimeFormatPreference.HOUR_24 -> true
        }
        val todayLogs = records
            .filter { it.timestamp in date.toStartOfDayTime..date.toEndOfDayTime }
            .sortedByDescending { it.timestamp }
            .map {
                val h = it.timestamp.hour
                val m = it.timestamp.minute
                val timeStr = if (is24Hour) {
                    "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
                } else {
                    val h12 = when {
                        h == 0 -> 12; h > 12 -> h - 12; else -> h
                    }
                    val period = if (h < 12) "AM" else "PM"
                    "${h12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} $period"
                }
                DrinkLogEntry(
                    time = timeStr,
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

    private val _firstDrinkDialogVisible = MutableStateFlow(false)
    private val _animationPending = MutableStateFlow(false)

    // Polls the alarm scheduler every second so the countdown is always fresh,
    // even after the alarm fires and reschedules to a new slot.
    // Emits (millisRemaining, isExact): millisRemaining = 0 means the alarm is firing right now.
    private val alarmCountdownFlow: Flow<Pair<Long?, Boolean>> = flow {
        while (true) {
            val exactMs = AlarmSchedulerV3.getMillisUntilExactTrigger(
                application,
                DrinkAlarmManagerV2.EXACT_ALARM_ID,
            )
            if (exactMs != null) {
                emit(Pair(exactMs, true))
            } else {
                val inexactMs = AlarmSchedulerV3.getMillisUntilInexactTrigger(
                    application,
                    DrinkAlarmManagerV2.INEXACT_ALARM_ID,
                )
                emit(Pair(inexactMs, false))
            }
            delay(1_000)
        }
    }

    val state = combine(
        extras,
        history,
        drinkStatsObserver.drinkStats,
        purchaseManager.isAdFree,
    ) { extras, history, drinkStats, isAdFree ->
        if (history == null || extras == null) {
            DrinkState.Loading
        } else {
            DrinkState.Data(
                isAdFree = isAdFree,
                drinkStreakDays = extras.streakData.drinkStreakDays,
                showStreakBadge = extras.streakData.drinkStreakDays > 0,
                quickDrinkSuggestions = extras.quickDrinkSuggestions,
                allDrinkSuggestions = extras.allDrinkSuggestions,
                todayLogs = history.todayLogs,
                drinkStats = drinkStats,
                alarmCountdownMs = null,
            )
        }
    }.combine(prefsStorage.getUnitPreference()) { drinkState, unitPref ->
        if (drinkState is DrinkState.Data) drinkState.copy(unitPreference = unitPref)
        else drinkState
    }.combine(alarmCountdownFlow) { drinkState, (alarmCountdownMs, alarmIsExact) ->
        if (drinkState is DrinkState.Data) drinkState.copy(
            alarmCountdownMs = alarmCountdownMs,
            alarmIsExact = alarmIsExact,
        )
        else drinkState
    }.combine(notificationPermissionObserver.hasPermission) { drinkState, hasPermission ->
        if (drinkState is DrinkState.Data && !hasPermission) drinkState.copy(alarmCountdownMs = null)
        else drinkState
    }.combine(activeHoursObserver.inActiveHours) { drinkState, inActiveHours ->
        if (drinkState is DrinkState.Data) drinkState.copy(isOutsideActiveHours = !inActiveHours)
        else drinkState
    }.combine(_firstDrinkDialogVisible) { drinkState, showDialog ->
        if (drinkState is DrinkState.Data) drinkState.copy(showFirstDrinkDialog = showDialog)
        else drinkState
    }.combine(_animationPending) { drinkState, pending ->
        if (drinkState is DrinkState.Data) drinkState.copy(drinkAnimationPending = pending)
        else drinkState
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DrinkState.Loading,
    )

    fun onAction(action: DrinkAction) {
        when (action) {
            is DrinkAction.Drink -> onDrink(amount = action.amount, source = action.source)
            is DrinkAction.CreatePreset -> onCreatePreset(amount = action.amount)
            is DrinkAction.DeletePreset -> onDeletePreset(action.preset)
            is DrinkAction.ReorderPresets -> onPresetsReordered(
                newOrder = action.newOrder,
                reorderingQuickPresets = action.reorderingQuickPresets
            )

            is DrinkAction.RestorePreset -> onRestorePreset(action.preset)
            is DrinkAction.BottomSheetOpened -> onBottomSheetOpened(presetCount = action.presetCount)
        }
    }

    fun onDrink(amount: Float, source: DrinkSource) {
        val sourceKey = when (source) {
            DrinkSource.QUICK_SUGGESTION -> TrackingDrink.SOURCE_QUICK_SUGGESTION
            DrinkSource.BOTTOM_SHEET_SUGGESTION -> TrackingDrink.SOURCE_BOTTOM_SHEET_SUGGESTION
            DrinkSource.BOTTOM_SHEET_CUSTOM -> TrackingDrink.SOURCE_BOTTOM_SHEET_CUSTOM
        }
        trackingService.trackDrinkRecorded(sourceKey, amount)

        viewModelScope.launch {
            val isFirstDrink = !prefsStorage.getFirstDrinkDialogSeen().first()

            withContext(Dispatchers.IO) {
                val record = HistoryEntity(
                    amount = amount,
                    status = Status.IN_APP,
                )

                dao.insertAll(record)

                drinkAlarmManagerV2.restoreAlarm(source = "drink-button")
            }

            val animEnabled = prefsStorage.getWaterAnimationEnabled().first()
            if (animEnabled && !_animationPending.value) {
                _animationPending.value = true
            }

            if (isFirstDrink) {
                prefsStorage.setFirstDrinkDialogSeen()
                val currentState = state.value
                if (currentState is DrinkState.Data) {
                    _firstDrinkDialogVisible.value = true
                }
            }
        }
    }

    fun onFirstDrinkDialogDismissed() {
        _firstDrinkDialogVisible.value = false
    }

    fun onAnimationDone() {
        _animationPending.value = false
    }

    fun onBottomSheetOpened(presetCount: Int) {
        trackingService.trackPresetBottomSheetOpened(presetCount)
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
                    trackingService.trackPresetCreated(amount, updated.size)
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
            val today = historyDate.value
            val records = withContext(Dispatchers.IO) {
                dao.findByTimestamp(
                    start = today.toStartOfDayTime,
                    end = today.toEndOfDayTime,
                ).firstOrNull() ?: emptyList()
            }
            val amountMl = records.sumOf { it.amount.toDouble() }.toFloat().coerceAtLeast(1f)
            val intent = GoalCelebrationOverlay.createIntent(
                context = application,
                style = CelebrationStyle.Ocean,
                amountMl = amountMl,
                drinkCount = records.size.coerceAtLeast(1),
                streakDays = streakRepository.streakData.firstOrNull()?.goalStreakDays ?: 0,
                unitPreference = currentState?.unitPreference ?: UnitPreference.ML,
            )
            application.startService(intent)
        }
    }

    companion object {

        const val QUICK_DRINK_PRESETS_AMOUNT = 3
        const val TODAY_LOGS_AMOUNT = 4
    }
}
