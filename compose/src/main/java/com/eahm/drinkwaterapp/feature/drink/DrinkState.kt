package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val username: String?,
        val currentStreak: Int,
        val drinkStats: DrinkStats?,
        val quickDrinkPresets: List<DrinkPreset>,
        val allDrinkPresets: List<DrinkPreset>,
        val todayLogs: List<DrinkLogEntry>,
    ) : DrinkState
}

data class DrinkStats(
    val goalAmount: Float,
    val drinkAmount: Float,
    val goalReached: Boolean,
) {
    val remainingAmount = (goalAmount - drinkAmount).coerceAtLeast(0f)
}

data class DrinkExtras(
    val username: String?,
    val quickDrinkPresets: List<DrinkPreset>,
    val allDrinkPresets: List<DrinkPreset>,
)
