package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import com.eahm.drinkwaterapp.core.data.drinkalarm.whattodo.WhatToDoNow
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

// UI state (Loading skeleton → Data)
internal sealed interface AlarmIntervalsState {
    data object Loading : AlarmIntervalsState

    data class Data(
        val now: LocalDateTime,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val goalMl: Float,
        val interval: AlarmInterval,
        val intervalMinutes: Int,
        val whatToDoResult: WhatToDoNow,
    ) : AlarmIntervalsState
}

// User intents (UI → ViewModel)
internal sealed interface AlarmIntervalsAction {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface AlarmIntervalsEvent {
    // events added as the feature grows
}
