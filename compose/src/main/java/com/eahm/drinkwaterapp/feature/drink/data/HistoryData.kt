package com.eahm.drinkwaterapp.feature.drink.data

import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry
import kotlinx.datetime.LocalDateTime

internal data class HistoryData(
    val date: LocalDateTime,
    val todayLogs: List<DrinkLogEntry>
)
