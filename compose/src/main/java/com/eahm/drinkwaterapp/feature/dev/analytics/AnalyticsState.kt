package com.eahm.drinkwaterapp.feature.dev.analytics

import androidx.compose.runtime.Immutable

// UI state (Loading skeleton → Data)
internal sealed interface AnalyticsState {

    data object Loading : AnalyticsState

    @Immutable
    data class Data(
        val versionName: String,
        val buildVariant: String,
    ) : AnalyticsState
}

// User intents (UI → ViewModel)
internal sealed interface AnalyticsAction {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface AnalyticsEvent {
    // events added as the feature grows
}
