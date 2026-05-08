package com.eahm.drinkwaterapp.core.data.streak

data class StreakData(
    val currentGoalStreak: Int = 0,
    val currentDrinkStreak: Int = 0,
    val bestGoalStreak: Int = 0,
    val bestDrinkStreak: Int = 0,
    val personalBestMl: Int = 0,
)
