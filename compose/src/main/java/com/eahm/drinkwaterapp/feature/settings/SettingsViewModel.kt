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
        activeTimes,
        _state
    ) { username, activeTimes, state ->
        state.copy(
            username = username,
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
                ?.let {
                    prefsStorage.setUsername(it)
                }
        }
    }

    fun onGoalChanged(goalMl: Int) {
        _state.update { it.copy(dailyGoalMl = goalMl) }
    }

    fun onUnitChanged(unit: WaterUnit) {
        _state.update { it.copy(unit = unit) }
    }

    fun onIntervalChanged(minutes: Int) {
        _state.update { it.copy(intervalMinutes = minutes) }
    }

    fun onStartTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            // TODO: validate range makes sense with end time
            val start = ActiveTime(time.hour, time.minute)
            prefsStorage.setStartTime(start)
            _state.update { it.copy(startTime = start) }
        }
    }

    fun onEndTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            // TODO: validate range makes sense with start time
            val end = ActiveTime(time.hour, time.minute)
            prefsStorage.setEndTime(end)
            _state.update { it.copy(endTime = end) }
        }
    }
}
