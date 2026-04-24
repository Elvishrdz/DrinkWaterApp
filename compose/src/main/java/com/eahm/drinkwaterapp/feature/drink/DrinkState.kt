package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val date: LocalDateTime,
        val records: List<HistoryEntity>,
        val goalAmount: String,
        val drinkAmount: Float,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DrinkState
}
