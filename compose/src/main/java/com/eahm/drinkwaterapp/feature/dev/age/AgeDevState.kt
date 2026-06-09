package com.eahm.drinkwaterapp.feature.dev.age

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.age.api.AgeSignals

@Immutable
internal data class AgeDevState(
    val storedSignal: AgeSignals? = null,
    val liveSignal: AgeSignals? = null,
    val needsAgeCollection: Boolean = false,
    val overrideLabel: String = REAL_API_LABEL,
) {
    companion object {
        const val REAL_API_LABEL = "Real Play API"
    }
}
