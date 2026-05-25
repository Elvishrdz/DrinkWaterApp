package com.eahm.drinkwaterapp.feature.dev.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.config.PrivacyPolicyConfig
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
        prefs.getPrivacyPolicyVersionAccepted(),
    ) { acceptedAt, trackingDecided, versionAccepted ->
        ConsentDevState.Data(
            privacyPolicyAcceptedAt = acceptedAt,
            trackingConsentDecided = trackingDecided,
            isTracking = trackingService.isTracking(),
            privacyPolicyVersionAccepted = versionAccepted,
            currentPolicyDate = PrivacyPolicyConfig.CURRENT_POLICY_DATE,
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

    fun setPrivacyPolicyAcceptedNow() {
        viewModelScope.launch {
            prefs.setPrivacyPolicyAccepted(System.currentTimeMillis())
        }
    }

    fun simulatePolicyUpdate() {
        viewModelScope.launch { prefs.clearPrivacyPolicyVersionAccepted() }
    }

    fun acceptPolicyVersionNow() {
        viewModelScope.launch { prefs.setPrivacyPolicyVersionAccepted(PrivacyPolicyConfig.CURRENT_POLICY_DATE) }
    }
}
