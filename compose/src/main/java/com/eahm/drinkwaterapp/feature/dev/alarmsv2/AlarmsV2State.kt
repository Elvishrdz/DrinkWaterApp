package com.eahm.drinkwaterapp.feature.dev.alarmsv2

internal sealed interface AlarmsV2State {

    data object Loading : AlarmsV2State

    data class Data(
        val exactAlarmActive: Boolean,
        val inexactAlarmActive: Boolean,
        val exactPermissionGranted: Boolean,
        val isWithinActiveHours: Boolean,
        val nextSlot: String,
        val currentTime: String,
    ) : AlarmsV2State
}
