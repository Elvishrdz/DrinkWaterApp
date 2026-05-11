package com.eahm.drinkwaterapp.feature.dev.streak

import kotlinx.datetime.LocalDate

internal sealed interface StreakDevState {
    data object Loading : StreakDevState

    data class Data(
        val currentGoalStreak: Int,
        val currentDrinkStreak: Int,
        val bestGoalStreak: Int?,
        val bestDrinkStreak: Int?,
        val personalBestMl: Int,
        val storedBestGoalStreak: Int,
        val storedBestDrinkStreak: Int,
        val selectedDate: LocalDate,
        val drinkAmountInput: Float,
    ) : StreakDevState
}
