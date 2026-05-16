package com.eahm.drinkwaterapp.feature.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ConsentViewModel(
    private val prefsStorage: PrefsStorage,
    private val trackingService: TrackingService,
) : ViewModel() {

    private val _state = MutableStateFlow<ConsentState>(ConsentState.Loading)
    val state: StateFlow<ConsentState> = _state

    private val _events = Channel<ConsentEvent>(Channel.Factory.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val privacyAcceptedAt = prefsStorage.getPrivacyPolicyAcceptedAt().firstOrNull()
            if (privacyAcceptedAt != null) {
                _state.update {
                    ConsentState.Data(
                        step = ConsentStep.TRACKING,
                        showStepCounter = false,
                        privacyToggleChecked = false,
                        isTrackingEnabled = trackingService.isTracking(),
                    )
                }
            } else {
                _state.update {
                    ConsentState.Data(
                        step = ConsentStep.PRIVACY_POLICY,
                        showStepCounter = true,
                        privacyToggleChecked = false,
                        isTrackingEnabled = trackingService.isTracking(),
                    )
                }
            }
        }
    }

    fun onAction(action: ConsentAction) {
        when (action) {
            is ConsentAction.SetPrivacyToggle -> {
                _state.update { current ->
                    (current as? ConsentState.Data)?.copy(privacyToggleChecked = action.checked)
                        ?: current
                }
            }

            ConsentAction.AcceptPrivacyPolicy -> {
                viewModelScope.launch {
                    prefsStorage.setPrivacyPolicyAccepted(System.currentTimeMillis())
                    _state.update { current ->
                        (current as? ConsentState.Data)?.copy(step = ConsentStep.TRACKING)
                            ?: current
                    }
                }
            }

            ConsentAction.AllowTracking -> {
                viewModelScope.launch {
                    trackingService.startTracking()
                    prefsStorage.setTrackingConsentDecided()
                    _events.send(ConsentEvent.ConsentCompleted)
                }
            }

            ConsentAction.DeclineTracking -> {
                viewModelScope.launch {
                    trackingService.stopTracking(TrackingService.Companion.OptOutSource.Welcome)
                    prefsStorage.setTrackingConsentDecided()
                    _events.send(ConsentEvent.ConsentCompleted)
                }
            }
        }
    }
}
