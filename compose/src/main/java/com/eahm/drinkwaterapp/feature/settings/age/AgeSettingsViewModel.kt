package com.eahm.drinkwaterapp.feature.settings.age

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeSignals
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the Settings "Age (ads)" entry: surfaces the current age signal and lets the user change a
 * **declared** age. API-sourced values are authoritative and shown read-only.
 */
internal class AgeSettingsViewModel(
    private val ageRepository: AgeRepository,
) : ViewModel() {

    val ageSignals: StateFlow<AgeSignals?> = ageRepository.ageSignals
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            ageRepository.ageSignals.value,
        )

    fun submitBirthYear(birthYear: Int) {
        viewModelScope.launch { ageRepository.submitDeclaredBirthYear(birthYear) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
