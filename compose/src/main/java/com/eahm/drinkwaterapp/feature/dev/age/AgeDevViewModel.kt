package com.eahm.drinkwaterapp.feature.dev.age

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.age.api.AgeDebugController
import com.eahm.drinkwaterapp.age.api.AgePreferences
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Developer-menu controller for the Play Age Signals feature. Lets us simulate API responses,
 * inspect stored vs. live state, drive the bootstrap age screen, and clear stored data — without a
 * real Texas account or a Play-distributed install.
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

    /** Simulate a Play API response, then re-run the repository's cold-start resolution. */
    fun simulate(preset: AgeSimulation) {
        viewModelScope.launch {
            debugController.simulate(preset.resolution)
            ageRepository.refresh()
            reload()
        }
    }

    /** Restore the real Play API and re-resolve. */
    fun useRealApi() {
        viewModelScope.launch {
            debugController.simulate(null)
            ageRepository.refresh()
            reload()
        }
    }

    /** Re-run resolution with the current override (no state change). */
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
                storedSignal = agePreferences.getAgeSignals(),
                liveSignal = ageRepository.ageSignals.value,
                needsAgeCollection = ageRepository.needsAgeCollection.value,
                overrideLabel = debugController.override?.describe() ?: AgeDevState.REAL_API_LABEL,
            )
        }
    }

    private fun AgeResolution.describe(): String = when (this) {
        is AgeResolution.Covered -> "Covered(${signals.status}, ${signals.adTreatment})"
        AgeResolution.CoveredAgeUnknown -> "CoveredAgeUnknown"
        AgeResolution.NotCovered -> "NotCovered"
        is AgeResolution.Undetermined -> "Undetermined($reason)"
    }
}

/** Preset Play API responses surfaced as buttons in the dev menu. */
internal enum class AgeSimulation(val label: String, val resolution: AgeResolution) {
    COVERED_ADULT(
        "Covered · Adult (Verified 18+)",
        AgeResolution.Covered(
            AgeSignals.fromApi(
                AgeStatus.VERIFIED,
                ageLower = 18,
                ageUpper = null
            )
        ),
    ),
    COVERED_TEEN(
        "Covered · Teen (Declared 13–17)",
        AgeResolution.Covered(AgeSignals.fromApi(AgeStatus.DECLARED, ageLower = 13, ageUpper = 17)),
    ),
    COVERED_CHILD(
        "Covered · Child (Supervised <13)",
        AgeResolution.Covered(
            AgeSignals.fromApi(
                AgeStatus.SUPERVISED,
                ageLower = 0,
                ageUpper = 12
            )
        ),
    ),
    COVERED_AGE_UNKNOWN(
        "Covered · Age unknown (UNKNOWN)",
        AgeResolution.CoveredAgeUnknown,
    ),
    NOT_COVERED(
        "Not covered (null)",
        AgeResolution.NotCovered,
    ),
    UNDETERMINED_NETWORK(
        "Undetermined · Network error",
        AgeResolution.Undetermined(com.eahm.drinkwaterapp.age.api.AgeError.NETWORK_ERROR),
    ),
    UNDETERMINED_NOT_OWNED(
        "Undetermined · App not owned",
        AgeResolution.Undetermined(com.eahm.drinkwaterapp.age.api.AgeError.APP_NOT_OWNED),
    ),
}
