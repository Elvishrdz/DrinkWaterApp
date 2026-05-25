package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ConsentState {

    data object Loading : ConsentState

    data class Data(
        val step: ConsentStep,
        val showStepCounter: Boolean,
        val showNewBadge: Boolean,
        val privacyToggleChecked: Boolean,
        val isTrackingEnabled: Boolean,
    ) : ConsentState
}

internal enum class ConsentStep {
    LEGAL,
    TRACKING_REQUEST,
}
