package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface AlarmsV2Action {

    // V2 dual alarm
    data object StartAlarms : AlarmsV2Action
    data object StopAlarms : AlarmsV2Action
    data object Refresh : AlarmsV2Action
    data object OpenExactPermissionSettings : AlarmsV2Action

    // V1 alarm (legacy)
    data object CallOnResume : AlarmsV2Action
    data object SetupV1Alarm : AlarmsV2Action
    data object StopV1Alarm : AlarmsV2Action

    // Notification permission
    data object NotificationPermissionGranted : AlarmsV2Action
    data object OpenAppPermissionSettings : AlarmsV2Action

    // Test alarm (AlarmSchedulerV2)
    data object ScheduleTestAlarm : AlarmsV2Action
    data object CancelTestAlarm : AlarmsV2Action
    data class OnTestAlarmTimeSelected(val time: TimeSelected) : AlarmsV2Action
}
