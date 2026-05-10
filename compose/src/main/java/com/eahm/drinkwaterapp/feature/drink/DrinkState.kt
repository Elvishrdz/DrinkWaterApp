package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.data.streak.StreakData
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry

internal sealed interface DrinkState {

    data object Loading : DrinkState

    data class Data(
        val currentStreak: Int,
        val showStreak: Boolean,
        val drinkStats: DrinkStats?,
        val quickDrinkSuggestions: List<DrinkSuggestion>,
        val allDrinkSuggestions: List<DrinkSuggestion>,
        val todayLogs: List<DrinkLogEntry>,
        val alarmTriggerAtMs: Long?,
    ) : DrinkState
}

data class DrinkExtras(
    val streakData: StreakData,
    val quickDrinkSuggestions: List<DrinkSuggestion>,
    val allDrinkSuggestions: List<DrinkSuggestion>,
)
