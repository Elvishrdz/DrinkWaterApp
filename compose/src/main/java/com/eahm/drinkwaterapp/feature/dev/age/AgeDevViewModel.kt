package com.eahm.drinkwaterapp.feature.dev.age

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.age.api.AgeDebugController
import com.eahm.drinkwaterapp.age.api.AgePreferences
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeSimulatedResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Developer-menu controller for the Play Age Signals feature. Lets us pin a fixed API response
 * (applied on the next startup), inspect stored vs. live state, drive the bootstrap age screen, and
 * clear stored data — without a real Texas account or a Play-distributed install. The override is
 * available only on debug builds.
 */
internal class AgeDevViewModel(
    private val ageRepository: AgeRepository,
    private val agePreferences: AgePreferences,
    private val debugController: AgeDebugController,
) : ViewModel() {

    private val _state = MutableStateFlow(AgeDevState())
    val state: StateFlow<AgeDevState> = _state.asStateFlow()

    init {
        reload()
    }

    /** Enable/disable using a pinned response instead of the real API. */
    fun setOverrideEnabled(enabled: Boolean) {
        viewModelScope.launch {
            debugController.setOverrideEnabled(enabled)
            ageRepository.refresh()
            reload()
        }
    }

    /** Pin the response used on the next startup (and apply it now via refresh). */
    fun selectResponse(response: AgeSimulatedResponse) {
        viewModelScope.launch {
            debugController.setSelectedResponse(response)
            if (response.clearsStoredAge) agePreferences.clearAgeSignals()
            ageRepository.refresh()
            reload()
        }
    }

    /** Re-run resolution with the current configuration. */
    fun refresh() {
        viewModelScope.launch {
            ageRepository.refresh()
            reload()
        }
    }

    /** Wipe stored age so the next resolution can trigger the bootstrap screen again. */
    fun clearStoredAge() {
        viewModelScope.launch {
            agePreferences.clearAgeSignals()
            reload()
        }
    }

    /** Persist a declared birth year (as the bootstrap screen would). */
    fun submitBirthYear(birthYear: Int) {
        viewModelScope.launch {
            ageRepository.submitDeclaredBirthYear(birthYear)
            reload()
        }
    }

    private fun reload() {
        viewModelScope.launch {
            _state.value = AgeDevState(
                overrideAvailable = debugController.isAvailable,
                overrideEnabled = debugController.isOverrideEnabled(),
                selectedResponse = debugController.getSelectedResponse(),
                storedSignal = agePreferences.getAgeSignals(),
                liveSignal = ageRepository.ageSignals.value,
                needsAgeCollection = ageRepository.needsAgeCollection.value,
            )
        }
    }
}
