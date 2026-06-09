package com.eahm.drinkwaterapp.feature.dev.age

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSimulatedResponse

@Immutable
internal data class AgeDevState(
    val overrideAvailable: Boolean = false,
    val overrideEnabled: Boolean = false,
    val selectedResponse: AgeSimulatedResponse? = null,
    val storedSignal: AgeSignals? = null,
    val liveSignal: AgeSignals? = null,
    val needsAgeCollection: Boolean = false,
)
