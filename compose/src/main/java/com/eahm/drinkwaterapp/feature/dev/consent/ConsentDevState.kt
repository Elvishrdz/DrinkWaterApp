package com.eahm.drinkwaterapp.feature.dev.consent

internal sealed interface ConsentDevState {
    data object Loading : ConsentDevState
    data class Data(
        val privacyPolicyAcceptedAt: Long?,
        val trackingConsentDecided: Boolean,
        val isTracking: Boolean,
    ) : ConsentDevState
}
