package com.eahm.drinkwaterapp.core.data.streak

import kotlinx.datetime.LocalDate

data class StreakData(
    val drinkStreakDays: Int = 0,
    val goalStreakDays: Int = 0,
    val bestDrinkStreakDays: Int? = null,
    val bestGoalStreakDays: Int? = null,
    val personalBestMl: Float? = null,
    val personalBestDate: LocalDate? = null,
)
