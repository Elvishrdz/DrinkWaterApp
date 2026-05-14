package com.eahm.drinkwaterapp.core.database.model

import androidx.room.ColumnInfo
import kotlinx.datetime.LocalDate

data class HighestDrinkDay(
    @ColumnInfo(name = "day") val day: LocalDate,
    @ColumnInfo(name = "total_drink_amount") val totalDrinkAmount: Float,
)
