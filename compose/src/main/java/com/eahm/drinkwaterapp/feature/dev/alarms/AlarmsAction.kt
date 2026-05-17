package com.eahm.drinkwaterapp.feature.dev.alarms

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface AlarmsAction {

    data object OnResumeCalled : AlarmsAction
    data object NotificationPermissionGranted : AlarmsAction
    data object OpenAppPermissionSettings : AlarmsAction
    data object SetupAlarm : AlarmsAction
    data object StopAlarm : AlarmsAction

    // Exact alarm test
    data object ScheduleTestAlarm : AlarmsAction
    data object CancelTestAlarm : AlarmsAction
    data object RefreshPermissionState : AlarmsAction
    data object OpenExactAlarmPermissionSettings : AlarmsAction
    data class OnTestAlarmTimeSelected(val time: TimeSelected) : AlarmsAction
}
