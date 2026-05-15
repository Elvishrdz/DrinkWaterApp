package com.eahm.drinkwaterapp.feature.dev.exactalarm

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface ExactAlarmDevAction {
    data object OpenPermissionSettings : ExactAlarmDevAction
    data object ScheduleTestAlarm : ExactAlarmDevAction
    data object CancelTestAlarm : ExactAlarmDevAction
    data object RefreshPermissionState : ExactAlarmDevAction
    data class OnTimeSelected(val time: TimeSelected) : ExactAlarmDevAction
}
