package com.eahm.drinkwaterapp.feature.dev.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService.Companion.OptOutSource
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.config.BuildEnvironment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TrackingDevViewModel(
    private val trackingService: TrackingService,
    private val appInfoService: AppInfoService,
) : ViewModel() {

    private val _state = MutableStateFlow<TrackingDevState>(TrackingDevState.Loading)
    val state: StateFlow<TrackingDevState> = _state

    init {
        _state.update {
            TrackingDevState.Data(
                isTrackingEnabled = trackingService.isTracking(),
                isUpdatingTracking = false,
                testEventParam = "",
                canSendTestEvent = !BuildEnvironment.isProduction,
                testEventSentFeedback = false,
                buildVariant = appInfoService.buildVariant,
                distinctId = trackingService.getDistinctId(),
            )
        }
    }

    fun onAction(action: TrackingDevAction) {
        when (action) {
            is TrackingDevAction.SetTrackingEnabled -> toggleTracking(action.enabled)
            is TrackingDevAction.SetTestEventParam -> updateTestParam(action.param)
            TrackingDevAction.SendTestEvent -> sendTestEvent()
            TrackingDevAction.FlushEvents -> trackingService.flush()
        }
    }

    private fun toggleTracking(enabled: Boolean) {
        _state.update { state ->
            (state as? TrackingDevState.Data)?.copy(isUpdatingTracking = true) ?: state
        }
        viewModelScope.launch {
            // Simulate the async opt-in/opt-out latency a real consent flow would have.
            delay(700)
            if (enabled) trackingService.startTracking()
            else trackingService.stopTracking(OptOutSource.DevMenu)
            _state.update { state ->
                (state as? TrackingDevState.Data)?.copy(
                    isUpdatingTracking = false,
                    isTrackingEnabled = enabled,
                ) ?: state
            }
        }
    }

    private fun updateTestParam(param: String) {
        _state.update { state ->
            (state as? TrackingDevState.Data)?.copy(testEventParam = param) ?: state
        }
    }

    private fun sendTestEvent() {
        val data = _state.value as? TrackingDevState.Data ?: return
        trackingService.trackTestEvent(data.testEventParam)
        viewModelScope.launch {
            _state.update { s ->
                (s as? TrackingDevState.Data)?.copy(testEventSentFeedback = true) ?: s
            }
            delay(2_000)
            _state.update { s ->
                (s as? TrackingDevState.Data)?.copy(testEventSentFeedback = false) ?: s
            }
        }
    }
}
