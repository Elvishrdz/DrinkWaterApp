package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

internal sealed interface AlarmsV2State {

    data object Loading : AlarmsV2State

    data class Data(
        val currentTime: String,

        // V2 exact alarm countdown
        // null  → alarm not scheduled
        // > 0   → milliseconds remaining
        // == 0  → countdown expired, show "will trigger soon"
        val exactAlarmActive: Boolean,
        val exactCountdownMs: Long?,
        val exactScheduledAt: String?,

        // V2 inexact alarm countdown
        val inexactAlarmActive: Boolean,
        val inexactCountdownMs: Long?,
        val inexactScheduledAt: String?,
        // Set when the inexact alarm fired before its scheduled time
        val inexactEarlyMessage: String?,

        // V1 alarm (legacy DrinkAlarmManager)
        val v1AlarmActive: Boolean,
        val v1AlarmStorageInfo: AlarmStorageInfo?,

        // Permissions
        val exactPermissionGranted: Boolean,
        val hasNotificationPermissions: Boolean,

        // System state
        val isWithinActiveHours: Boolean,
        val nextSlot: String,

        // Test alarm (AlarmSchedulerV2)
        val isTestAlarmActive: Boolean,
        val scheduledAsExact: Boolean?,
        val testCountdownMs: Long?,
        val selectedTestTime: TimeSelected,
        val lastPermissionEvent: String?,
    ) : AlarmsV2State
}
