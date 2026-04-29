package com.eahm.drinkwaterapp.feature.drink.data

import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

internal data class HistoryData(
    val date: LocalDateTime,
    val historyRecords: List<HourGroup>,
    val todayLogs: List<DrinkLogEntry>
)
