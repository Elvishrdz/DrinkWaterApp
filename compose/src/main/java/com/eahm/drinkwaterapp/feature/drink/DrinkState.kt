package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity

internal data class DrinkState(
    val records: List<HistoryEntity>,
    val isAlarmActive: Boolean,
    val alarmInterval: Long?,
    val alarmRemainingTime: String,
)
