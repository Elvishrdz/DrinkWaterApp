package com.eahm.drinkwaterapp.feature.dev.exactalarm

import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface ExactAlarmDevState {
    data object Loading : ExactAlarmDevState
    data class Data(
        val isPermissionGranted: Boolean,
        val isTestAlarmActive: Boolean,
        val scheduledAsExact: Boolean?,
        val selectedTime: TimeSelected,
        val countdownMs: Long?,
        val lastPermissionEvent: String?,
    ) : ExactAlarmDevState
}
