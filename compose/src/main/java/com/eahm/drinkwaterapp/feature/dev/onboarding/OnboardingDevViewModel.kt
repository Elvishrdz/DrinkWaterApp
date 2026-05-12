package com.eahm.drinkwaterapp.feature.dev.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class OnboardingDevViewModel(
    private val prefs: PrefsStorage,
) : ViewModel() {

    val state: StateFlow<OnboardingDevState> = prefs.getOnboardingCompleted()
        .map { OnboardingDevState.Data(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingDevState.Loading)

    fun resetOnboarding() {
        viewModelScope.launch { prefs.setOnboardingCompleted(false) }
    }
}
