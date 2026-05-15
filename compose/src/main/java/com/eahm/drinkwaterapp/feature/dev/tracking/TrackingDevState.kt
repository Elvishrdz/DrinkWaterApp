package com.eahm.drinkwaterapp.feature.dev.tracking

import androidx.compose.runtime.Immutable

// UI state
internal sealed interface TrackingDevState {

    data object Loading : TrackingDevState

    @Immutable
    data class Data(
        val isTrackingEnabled: Boolean,
        /** True while the opt-in / opt-out call is in flight (suspend state for the toggle). */
        val isUpdatingTracking: Boolean,
        val testEventParam: String,
        /** False in production — test events must never reach the live Mixpanel project. */
        val canSendTestEvent: Boolean,
        /** Brief true pulse after a test event is successfully dispatched. */
        val testEventSentFeedback: Boolean,
        val buildVariant: String,
    ) : TrackingDevState
}

// User intents
internal sealed interface TrackingDevAction {
    data class SetTrackingEnabled(val enabled: Boolean) : TrackingDevAction
    data class SetTestEventParam(val param: String) : TrackingDevAction
    data object SendTestEvent : TrackingDevAction
}
