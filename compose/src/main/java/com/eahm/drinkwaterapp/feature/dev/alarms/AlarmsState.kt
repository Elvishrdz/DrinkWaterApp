package com.eahm.drinkwaterapp.feature.dev.alarms

internal sealed interface AlarmsState {

    data object Loading : AlarmsState

    data class Data(
        val hasNotificationPermissions: Boolean,
        val isAlarmActive: Boolean,
    ) : AlarmsState
}

