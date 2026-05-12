package com.eahm.drinkwaterapp.feature.dev.notifications

// UI state (Loading skeleton → Data)
internal sealed interface NotificationsState {
    data object Loading : NotificationsState
    class Data(
        // fields added as the feature grows
    ) : NotificationsState
}

// User intents (UI → ViewModel)
internal sealed interface NotificationsAction {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface NotificationsEvent {
    // events added as the feature grows
}
