package com.eahm.drinkwaterapp.feature.dev.alarmsv2

internal sealed interface AlarmsV2Action {

    data object StartAlarms : AlarmsV2Action
    data object StopAlarms : AlarmsV2Action
    data object Refresh : AlarmsV2Action
    data object OpenExactPermissionSettings : AlarmsV2Action
}
