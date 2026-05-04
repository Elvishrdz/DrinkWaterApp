package com.eahm.drinkwaterapp.feature.dev.alarms

import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo

internal sealed interface AlarmsState {

    data object Loading : AlarmsState

    data class Data(
        val hasNotificationPermissions: Boolean,
        val isAlarmActive: Boolean,
        val alarmStorageInfo: AlarmStorageInfo?,
    ) : AlarmsState
}

