package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

// UI state (Loading skeleton → Data)
internal sealed interface AlarmIntervalsState {
    data object Loading : AlarmIntervalsState

    data class Data(
        val time: LocalDateTime,
        val now: LocalDateTime,
        val customTime: LocalDateTime?,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val goalMl: Float,
        val interval: AlarmInterval,
        val intervalMinutes: Int,
        val whatToDoResult: WhatToDoNow,
        val schedule: DrinkSchedule?,
        val nextAlarmSlot: LocalDateTime?,
    ) : AlarmIntervalsState
}

internal sealed interface AlarmIntervalsAction {

    data class SetCustomTime(val dateTime: LocalDateTime) : AlarmIntervalsAction
    data object ClearCustomTime : AlarmIntervalsAction
}

// One-shot side effects (ViewModel → UI)
internal sealed interface AlarmIntervalsEvent {
    // events added as the feature grows
}
