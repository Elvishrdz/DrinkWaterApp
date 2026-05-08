package com.eahm.drinkwaterapp.feature.settings

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.feature.drink.DateTimeObserver
import com.eahm.drinkwaterapp.feature.settings.data.ActiveTimes
import com.eahm.drinkwaterapp.ui.inputfield.validateText
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class SettingsViewModel(
    val application: Application,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val drinkAlarmManager: DrinkAlarmManager,
    val dateTimeObserver: DateTimeObserver,
    notificationPermissionObserver: NotificationPermissionObserver,
) : ViewModel() {

    private val dao = database.getDailyGoalDao()

    private val activeTimes = combine(
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { startTime, endTime ->
        ActiveTimes(startTime, endTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val reminderInfo = combine(
        activeTimes,
        prefsStorage.getInterval(),
        prefsStorage.getAmountPerDrink(),
    ) { activeTimes, interval, notificationAmountMl ->
        Triple(activeTimes, interval, notificationAmountMl)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val canDrawOverlay get() = Settings.canDrawOverlays(application)

    private val _state =
        MutableStateFlow(
            SettingsState(
                username = null,
                notificationsEnabled = notificationPermissionObserver.hasPermission.value,
                startTime = null,
                endTime = null,
                intervalMinutes = 0,
                goalReachedCelebrationsEnabled = canDrawOverlay,
            )
        )

    val today = dateTimeObserver.currentDateTime

    @OptIn(ExperimentalCoroutinesApi::class)
    private val goal = dateTimeObserver.currentDateTime
        .flatMapLatest { today ->
            dao.getGoalForDate(today.date.toEpochDays())
        }
        .map { (it?.goalMl ?: DEFAULT_GOAL_ML).toInt() }
        .flowOn(Dispatchers.IO)

    val state = combine(
        prefsStorage.getUsername(),
        goal,
        reminderInfo,
        notificationPermissionObserver.hasPermission,
        _state,
    ) { username, goal, reminderInfo, hasNotificationPermission, state ->
        state.copy(
            username = username,
            dailyGoalMl = goal,
            notificationsEnabled = hasNotificationPermission,
            intervalMinutes = reminderInfo?.second?.minute ?: 0,
            startTime = reminderInfo?.first?.start,
            endTime = reminderInfo?.first?.end,
            notificationAmountMl = reminderInfo?.third ?: 150f,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = _state.value,
    )

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun onUsernameChanged(username: String) {
        viewModelScope.launch {
            username
                .takeIf { validateText(it).isValid }
                ?.let { prefsStorage.setUsername(it) }
        }
    }

    fun onGoalChanged(goalMl: Int) {
        viewModelScope.launch {
            setGoalFromToday(goalMl.toFloat())
        }
    }

    fun onUnitChanged(unit: WaterUnit, weightUnit: WeightUnit) {
        _state.update { it.copy(unit = unit, weightUnit = weightUnit) }
    }

    fun onIntervalChanged(minutes: Int) {
        viewModelScope.launch {
            _state.update { it.copy(intervalMinutes = minutes) }
            prefsStorage.setInterval(ActiveTime(0, minutes))
            drinkAlarmManager.determineAction(AlarmSource.NotificationActiveHoursStartTimeChanged)
        }
    }

    fun onNotificationAmountChanged(ml: Float) {
        viewModelScope.launch {
            prefsStorage.setAmountPerDrink(ml)
        }
    }

    fun onStartTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            val newStart = ActiveTime(time.hour, time.minute)
            val currentEnd = state.value.endTime

            val error = currentEnd?.let { validateTimeRange(newStart, it) }
            if (error != null) {
                _state.update { it.copy(timeRangeError = error) }
                return@launch
            }

            _state.update { it.copy(timeRangeError = null) }
            prefsStorage.setStartTime(newStart)
            drinkAlarmManager.determineAction(AlarmSource.NotificationActiveHoursStartTimeChanged)
        }
    }

    fun onEndTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            val newEnd = ActiveTime(time.hour, time.minute)
            val currentStart = state.value.startTime

            val error = currentStart?.let { validateTimeRange(it, newEnd) }
            if (error != null) {
                _state.update { it.copy(timeRangeError = error) }
                return@launch
            }

            _state.update { it.copy(timeRangeError = null) }
            prefsStorage.setEndTime(newEnd)
            drinkAlarmManager.determineAction(AlarmSource.NotificationActiveHoursEndTimeChanged)
        }
    }

    fun onDrawOverlayPermissionResult() {
        _state.update { it.copy(goalReachedCelebrationsEnabled = canDrawOverlay) }
    }

    private suspend fun setGoalFromToday(goalMl: Float) {
        val current = today.value.date
        val epochDay = current.toEpochDays()
        val existing = dao.getGoalOnDate(epochDay).first()
        if (existing != null) dao.update(existing.copy(goalMl = goalMl))
        else dao.insert(DailyGoalEntity(effectiveDate = current, goalMl = goalMl))
    }

    /**
     * Returns a [TimeRangeError] if the [start]/[end] pair is invalid, null if the range is fine.
     *
     * Rules:
     * - [start] == [end] (same minute) → [TimeRangeError.SAME_TIME] — zero-duration window.
     * - [start] < [end] → normal same-day window, always valid.
     * - [start] > [end] → midnight-spanning window (e.g. 23:00 → 02:00), always valid.
     */
    private fun validateTimeRange(start: ActiveTime, end: ActiveTime): TimeRangeError? {
        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        return if (startMinutes == endMinutes) TimeRangeError.SAME_TIME else null
    }
}
