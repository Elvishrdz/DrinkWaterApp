package com.eahm.drinkwaterapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val drinkAlarmManager: DrinkAlarmManager,
    private val drinkAlarmManagerV2: DrinkAlarmManagerV2,
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    internal val state = MutableStateFlow<MainState>(MainState.Startup)

    init {
        updateState()
    }

    fun onResume() {
        viewModelScope.launch {
            drinkAlarmManager.onResume()
            drinkAlarmManagerV2.restoreAlarm(source = "onResume")
        }
    }

    fun onOnboardingCompleted() {
        updateState()
    }

    fun onConsentCompleted() {
        updateState()
    }

    private fun updateState() {
        viewModelScope.launch {
            state.update {
                MainState.Ready(
                    consentCompleted = prefsStorage.getTrackingConsentDecided().firstOrNull()
                        ?: false,
                    onboardingCompleted = prefsStorage.getOnboardingCompleted().firstOrNull()
                        ?: false,
                )
            }
        }
    }
}
