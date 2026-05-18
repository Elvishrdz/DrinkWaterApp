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
        // Exact alarm countdown
        // null  → alarm not scheduled
        // > 0   → milliseconds remaining
        // == 0  → countdown expired, show "will trigger soon"
        val exactCountdownMs: Long?,
        val exactScheduledAt: String?,
        // Inexact alarm countdown
        // null  → alarm not scheduled
        // > 0   → milliseconds remaining
        // == 0  → countdown expired, show "will trigger anytime from now"
        val inexactCountdownMs: Long?,
        val inexactScheduledAt: String?,
        // Set when the inexact alarm fired before its scheduled time
        val inexactEarlyMessage: String?,
    ) : AlarmsV2State
}
