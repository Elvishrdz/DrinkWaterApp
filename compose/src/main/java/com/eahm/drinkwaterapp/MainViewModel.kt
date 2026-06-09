package com.eahm.drinkwaterapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.core.config.PrivacyPolicyTermsOfServiceConfig
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val drinkAlarmManagerV2: DrinkAlarmManagerV2,
    private val prefsStorage: PrefsStorage,
    private val ageRepository: AgeRepository,
) : ViewModel() {

    internal val state = MutableStateFlow<MainState>(MainState.Startup)

    init {
        // Build the initial state immediately so the splash doesn't wait on the age API.
        updateState()
        // Resolve age signals once per cold start; the gate reacts when this completes.
        viewModelScope.launch {
            ageRepository.refresh()
            updateState()
        }
        // Keep the gate in sync if the resolution flips later.
        viewModelScope.launch {
            ageRepository.needsAgeCollection.collect { required ->
                state.update { current ->
                    if (current is MainState.Ready) {
                        current.copy(ageCollectionRequired = required)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            drinkAlarmManagerV2.restoreAlarm(source = "onResume")
        }
    }

    fun onOnboardingCompleted() {
        updateState()
    }

    fun onConsentCompleted() {
        updateState()
    }

    fun onAgeCollected() {
        updateState()
    }

    @Suppress("Indentation")
    private fun updateState() {
        viewModelScope.launch {
            val trackingDecided = prefsStorage.getTrackingConsentDecided().firstOrNull() ?: false
            val policyVersionAccepted = prefsStorage.getPrivacyPolicyVersionAccepted().firstOrNull()
            state.update {
                MainState.Ready(
                    consentCompleted = trackingDecided &&
                            policyVersionAccepted == PrivacyPolicyTermsOfServiceConfig.CURRENT_POLICY_DATE,
                    onboardingCompleted = prefsStorage.getOnboardingCompleted().firstOrNull()
                        ?: false,
                    ageCollectionRequired = ageRepository.needsAgeCollection.value,
                )
            }
        }
    }
}
