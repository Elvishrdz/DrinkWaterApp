package com.eahm.drinkwaterapp.feature.dev.notifications

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface NotificationsAction {

    data object ShowNotification : NotificationsAction
    data object ShowNotificationUsingBroadcast : NotificationsAction
    data class OnExtraTimeChanged(val time: TimeSelected) : NotificationsAction
}
