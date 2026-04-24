package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
        val goalAmount: String,
        val drinkAmount: Float,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DrinkState
}
