package com.eahm.drinkwaterapp.feature.drink.alarm

import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import kotlinx.datetime.LocalDateTime

internal sealed interface HomeAlarmState {

    data object Loading : HomeAlarmState

    /** Notification permission not granted. */
    data object NoPermission : HomeAlarmState

    /** Permission granted but active hours are not configured (start == end or defaults). */
    data object NotConfigured : HomeAlarmState

    /** Alarm is fully configured and ready. */
    data class Active(
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val interval: AlarmInterval,
        val schedule: DrinkSchedule?,
        val nextAlarmSlot: LocalDateTime?,
        val isInActiveHours: Boolean,
        val now: LocalDateTime,
        val timeFormatPreference: TimeFormatPreference = TimeFormatPreference.SYSTEM,
        /** True when the app is using inexact (battery-friendly) alarms instead of exact alarms. */
        val isInexact: Boolean = false,
    ) : HomeAlarmState
}
