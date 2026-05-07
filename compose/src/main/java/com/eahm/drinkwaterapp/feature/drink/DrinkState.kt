package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val username: String?,
        val currentStreak: Int,
        val drinkStats: DrinkStats?,
        val quickDrinkSuggestions: List<DrinkSuggestion>,
        val allDrinkSuggestions: List<DrinkSuggestion>,
        val todayLogs: List<DrinkLogEntry>,
        val alarmTriggerAtMs: Long?,
    ) : DrinkState
}

data class DrinkExtras(
    val username: String?,
    val quickDrinkSuggestions: List<DrinkSuggestion>,
    val allDrinkSuggestions: List<DrinkSuggestion>,
)
