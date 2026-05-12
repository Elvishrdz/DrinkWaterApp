package com.eahm.drinkwaterapp.feature.dev.notifications

// UI state (Loading skeleton → Data)
internal sealed interface NotificationsState {
    data object Loading : NotificationsState
    data class Data(val hasNotificationPermissions: Boolean) : NotificationsState
}

internal sealed interface NotificationsAction {

    data object ShowNotification : NotificationsAction
    data object ShowNotificationUsingBroadcast : NotificationsAction
}

// One-shot side effects (ViewModel → UI)
internal sealed interface NotificationsEvent {
    // events added as the feature grows
}
