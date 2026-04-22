package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val records: List<HistoryEntity>,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DrinkState
}
