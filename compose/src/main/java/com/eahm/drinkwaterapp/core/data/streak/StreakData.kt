package com.eahm.drinkwaterapp.core.data.streak

data class StreakData(
    val drinkStreakDays: Int = 0,
    val goalStreakDays: Int = 0,
    val bestDrinkStreakDays: Int? = null,
    val bestGoalStreakDays: Int? = null,
    val personalBestMl: Int = 0,
    // TODO: val personalBestDate: LocalDateTime,
)
