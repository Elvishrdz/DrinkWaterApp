package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.DrinkSource
import com.eahm.drinkwaterapp.feature.drink.DrinkState
import com.eahm.drinkwaterapp.feature.drink.drinkSuggestionsMocks

@Composable
internal fun ActionsSection(
    state: DrinkState.Data,
    onDrink: (Float, DrinkSource) -> Unit,
    onReorderPresets: (List<DrinkSuggestion>, Boolean) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onDeletePreset: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    onBottomSheetOpened: (Int) -> Unit,
) {
    QuickAddRow(
        quickPresets = state.quickDrinkSuggestions,
        allPresets = state.allDrinkSuggestions,
        onDrink = onDrink,
        onCreatePreset = onCreatePreset,
        onReorder = onReorderPresets,
        onDelete = onDeletePreset,
        onRestorePreset = onRestorePreset,
        onBottomSheetOpened = onBottomSheetOpened,
        unitPreference = state.unitPreference,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}

@Preview
@Composable
private fun ActionsSectionPreview() {
    DrinkWaterAppTheme {
        ActionsSection(
            state = DrinkState.Data(
                drinkStreakDays = 5,
                showStreakBadge = true,
                drinkStats = null,
                quickDrinkSuggestions = drinkSuggestionsMocks,
                allDrinkSuggestions = drinkSuggestionsMocks,
                todayLogs = emptyList(),
                alarmCountdownMs = null,
            ),
            onDrink = { _, _ -> },
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onBottomSheetOpened = {},
        )
    }
}
