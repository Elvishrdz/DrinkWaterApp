package com.eahm.drinkwaterapp.feature.dev.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class ConsentDevViewModel(
    private val prefs: PrefsStorage,
    private val trackingService: TrackingService,
) : ViewModel() {

    val state: StateFlow<ConsentDevState> = combine(
        prefs.getPrivacyPolicyAcceptedAt(),
        prefs.getTrackingConsentDecided(),
    ) { acceptedAt, trackingDecided ->
        ConsentDevState.Data(
            privacyPolicyAcceptedAt = acceptedAt,
            trackingConsentDecided = trackingDecided,
            isTracking = trackingService.isTracking(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConsentDevState.Loading)

    fun resetPrivacyPolicy() {
        viewModelScope.launch { prefs.clearPrivacyPolicyAccepted() }
    }

    fun resetTrackingConsentDecided() {
        viewModelScope.launch { prefs.clearTrackingConsentDecided() }
    }

    fun resetAll() {
        viewModelScope.launch {
            prefs.clearPrivacyPolicyAccepted()
            prefs.clearTrackingConsentDecided()
        }
    }


    /**
     * Sets the privacy policy timestamp to now and clears the tracking decision.
     * Simulates the "app killed after step 1" scenario: consent screen will open
     * directly at the tracking step (no step counter shown).
     */
    fun simulateReturnAtTrackingStep() {
        viewModelScope.launch {
            prefs.clearTrackingConsentDecided()
            prefs.setPrivacyPolicyAccepted(System.currentTimeMillis())
        }
    }
}
