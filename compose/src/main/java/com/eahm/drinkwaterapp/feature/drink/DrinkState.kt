package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
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

data class DrinkExtras(
    val username: String?,
    val quickDrinkPresets: List<DrinkPreset>,
    val allDrinkPresets: List<DrinkPreset>,
)
