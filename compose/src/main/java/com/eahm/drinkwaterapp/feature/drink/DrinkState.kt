package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
        val drinkStats: DrinkStats?,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DrinkState
}

data class DrinkStats(
    val goalAmount: Float,
    val drinkAmount: Float,
    val goalReached: Boolean,
)
