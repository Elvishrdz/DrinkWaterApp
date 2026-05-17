package com.eahm.drinkwaterapp.feature.dev.alarms

import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface AlarmsState {

    data object Loading : AlarmsState

    data class Data(
        val canScheduleExactAlarms: Boolean,
        val hasNotificationPermissions: Boolean,
        val isAlarmActive: Boolean,
        val alarmStorageInfo: AlarmStorageInfo?,
        val inActiveHours: Boolean,
        val isTestAlarmActive: Boolean,
        val scheduledAsExact: Boolean?,
        val countdownMs: Long?,
        val selectedTime: TimeSelected,
        val lastPermissionEvent: String?,
    ) : AlarmsState
}
