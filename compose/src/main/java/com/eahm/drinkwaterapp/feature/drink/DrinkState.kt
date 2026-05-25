package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.data.streak.StreakData
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkLogEntry

internal sealed interface DrinkState {

    data object Loading : DrinkState

    @Immutable
    data class Data(
        val isAdFree: Boolean,
        val drinkStreakDays: Int,
        val showStreakBadge: Boolean,
        val drinkStats: DrinkStats?,
        val quickDrinkSuggestions: List<DrinkSuggestion>,
        val allDrinkSuggestions: List<DrinkSuggestion>,
        val todayLogs: List<DrinkLogEntry>,
        val alarmCountdownMs: Long?,
        val alarmIsExact: Boolean = true,
        val unitPreference: UnitPreference = UnitPreference.ML,
        val isOutsideActiveHours: Boolean = false,
        val showFirstDrinkDialog: Boolean = false,
        val drinkAnimationPending: Boolean = false,
    ) : DrinkState
}

@Immutable
data class DrinkExtras(
    val streakData: StreakData,
    val quickDrinkSuggestions: List<DrinkSuggestion>,
    val allDrinkSuggestions: List<DrinkSuggestion>,
)

enum class DrinkSource {
    /** Quick-preset chip on the main drink screen. */
    QUICK_SUGGESTION,

    /** Preset chip tapped inside the bottom sheet grid. */
    BOTTOM_SHEET_SUGGESTION,

    /** Custom amount entered and confirmed via the bottom sheet's "Drink" button. */
    BOTTOM_SHEET_CUSTOM,
}
