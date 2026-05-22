package com.eahm.drinkwaterapp.feature.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService.Companion.OptOutSource
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ConsentViewModel(
    private val prefsStorage: PrefsStorage,
    private val trackingService: TrackingService,
) : ViewModel() {

    private val _events = Channel<ConsentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val showStepCounter = MutableStateFlow<Boolean?>(null)
    private val privacyToggleChecked = MutableStateFlow(false)
    private val isTrackingEnabled = prefsStorage.getTrackingConsentDecided()
        .map { trackingConsentDecided ->
            // if we don't have the user decision, we ask again even if tracking is already active
            trackingConsentDecided && trackingService.isTracking()
        }

    val state: StateFlow<ConsentState> = combine(
        showStepCounter,
        prefsStorage.getPrivacyPolicyAcceptedAt(),
        isTrackingEnabled,
        privacyToggleChecked,
    ) { showStepCounter, privacyPolicy, isTrackingEnabled, toggle ->
        if (privacyPolicy != null) {
            ConsentState.Data(
                step = ConsentStep.TRACKING_REQUEST,
                showStepCounter = showStepCounter == true,
                privacyToggleChecked = toggle,
                isTrackingEnabled = isTrackingEnabled,
            )

        } else {
            ConsentState.Data(
                step = ConsentStep.LEGAL,
                showStepCounter = showStepCounter == true,
                privacyToggleChecked = toggle,
                isTrackingEnabled = isTrackingEnabled,
            )
        }

    }.onStart {
        showStepCounter.value = prefsStorage.getPrivacyPolicyAcceptedAt().firstOrNull() == null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConsentState.Loading,
    )

    fun onAction(action: ConsentAction) {
        when (action) {
            is ConsentAction.SetPrivacyToggle -> {
                privacyToggleChecked.update { action.checked }
            }

            ConsentAction.AcceptPrivacyPolicy -> {
                viewModelScope.launch {
                    prefsStorage.setPrivacyPolicyAccepted(System.currentTimeMillis())
                }
            }

            ConsentAction.AllowTracking -> {
                viewModelScope.launch {
                    setTrackingConsentDecided(accepted = true)
                    onCompleteSteps()
                }
            }

            ConsentAction.DeclineTracking -> {
                viewModelScope.launch {
                    setTrackingConsentDecided(accepted = false)
                    onCompleteSteps()
                }
            }
        }
    }

    private suspend fun onCompleteSteps() {
        showStepCounter.value = null
        privacyToggleChecked.value = false
        _events.send(ConsentEvent.ConsentCompleted)
    }

    private suspend fun setTrackingConsentDecided(accepted: Boolean) {
        if (accepted) {
            trackingService.startTracking()
        } else {
            trackingService.stopTracking(OptOutSource.Welcome)
        }

        prefsStorage.setTrackingConsentDecided()
    }
}
