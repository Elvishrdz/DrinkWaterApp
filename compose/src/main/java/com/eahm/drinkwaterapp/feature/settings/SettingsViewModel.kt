package com.eahm.drinkwaterapp.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onNotificationsToggled(enabled: Boolean) {
        _state.update { it.copy(notificationsEnabled = enabled) }
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

    fun onStartTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(startHour = hour, startMinute = minute) }
    }

    fun onEndTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(endHour = hour, endMinute = minute) }
    }
}
