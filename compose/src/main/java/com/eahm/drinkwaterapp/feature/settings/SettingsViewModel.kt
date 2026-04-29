package com.eahm.drinkwaterapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.ui.inputfield.validateText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(username = null))
    val state = combine(
        prefsStorage.getUsername(),
        _state
    ) { username, state ->
        state.copy(
            username = username,
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

    fun onStartTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(startHour = hour, startMinute = minute) }
    }

    fun onEndTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(endHour = hour, endMinute = minute) }
    }
}
