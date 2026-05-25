package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.DrinkAction
import com.eahm.drinkwaterapp.feature.drink.DrinkState
import com.eahm.drinkwaterapp.feature.drink.drinkSuggestionsMocks

@Composable
internal fun ActionsSection(
    state: DrinkState.Data,
    onAction: (DrinkAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    QuickAddRow(
        quickPresets = state.quickDrinkSuggestions,
        allPresets = state.allDrinkSuggestions,
        onDrink = { amount, source -> onAction(DrinkAction.Drink(amount, source)) },
        onCreatePreset = { amount -> onAction(DrinkAction.CreatePreset(amount)) },
        onReorder = { newOrder, reorderingQuickPresets ->
            onAction(DrinkAction.ReorderPresets(newOrder, reorderingQuickPresets))
        },
        onDelete = { onAction(DrinkAction.DeletePreset(it)) },
        onRestorePreset = { onAction(DrinkAction.RestorePreset(it)) },
        onBottomSheetOpened = { onAction(DrinkAction.BottomSheetOpened(it)) },
        unitPreference = state.unitPreference,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ActionsSectionPreview() {
    DrinkWaterAppTheme {
        ActionsSection(
            state = DrinkState.Data(
                isAdFree = false,
                drinkStreakDays = 5,
                showStreakBadge = true,
                drinkStats = null,
                quickDrinkSuggestions = drinkSuggestionsMocks,
                allDrinkSuggestions = drinkSuggestionsMocks,
                todayLogs = emptyList(),
                alarmCountdownMs = null,
            ),
            onAction = {},
        )
    }
}
