package com.eahm.drinkwaterapp.feature.dev.alarms

internal sealed interface AlarmsAction {

    data object OnResumeCalled : AlarmsAction
    data object NotificationPermissionGranted : AlarmsAction
    data object OpenAppPermissionSettings : AlarmsAction
    data object SetupAlarm : AlarmsAction
    data object StopAlarm : AlarmsAction
}
