package com.eahm.drinkwaterapp.feature.dev.notifications

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface NotificationsState {

    data object Loading : NotificationsState

    data class Data(
        val hasNotificationPermissions: Boolean,
        val extraTime: TimeSelected,
    ) : NotificationsState
}
