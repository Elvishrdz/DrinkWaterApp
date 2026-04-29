package com.eahm.drinkwaterapp.feature.dev

import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.feature.drink.DrinkStats
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal sealed interface DevState {

    data object Loading : DevState

    data class Data(
        val username: String,
        val currentStreak: Int,
        val date: LocalDateTime,
        val historyRecords: List<HourGroup>,
        val todayLogs: List<DrinkLogEntry>,
        val drinkStats: DrinkStats?,
        val isAlarmActive: Boolean,
        val alarmInterval: AlarmInterval,
        val alarmNextInterval: AlarmInterval?,
    ) : DevState
}
