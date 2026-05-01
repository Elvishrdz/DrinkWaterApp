package com.eahm.drinkwaterapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.ui.inputfield.validateText
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveTimes(
    val start: ActiveTime,
    val end: ActiveTime,
)

internal class SettingsViewModel(
    val prefsStorage: PrefsStorage,
) : ViewModel() {

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

    private val _state =
        MutableStateFlow(SettingsState(username = null, startTime = null, endTime = null))

    val state = combine(
        prefsStorage.getUsername(),
        prefsStorage.getGoalAmount(),
        activeTimes,
        _state,
    ) { username, goal, activeTimes, state ->
        state.copy(
            username = username,
            dailyGoalMl = goal.toInt(),
            startTime = activeTimes?.start,
            endTime = activeTimes?.end,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = _state.value,
    )

    fun onNotificationsToggled(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
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
            prefsStorage.setGoalAmount(goalMl.toFloat())
        }
    }

    fun onUnitChanged(unit: WaterUnit, weightUnit: WeightUnit) {
        _state.update { it.copy(unit = unit, weightUnit = weightUnit) }
    }

    fun onIntervalChanged(minutes: Int) {
        _state.update { it.copy(intervalMinutes = minutes) }
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
        }
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
