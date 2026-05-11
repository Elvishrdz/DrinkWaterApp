package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.drinkSuggestionsMocks

@Composable
fun QuickAddRow(
    quickPresets: List<DrinkSuggestion>,
    allPresets: List<DrinkSuggestion>,
    onDrink: (Float) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onReorder: (List<DrinkSuggestion>, Boolean) -> Unit,
    onDelete: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    DraggableQuickAddRow(
        presets = quickPresets,
        onDrink = onDrink,
        onReorder = onReorder,
        modifier = modifier,
        trailingContent = { trailingModifier ->
            AddDrinkButton(
                presets = allPresets,
                onDrink = onDrink,
                onCreatePreset = onCreatePreset,
                onReorder = onReorder,
                onDelete = onDelete,
                onRestorePreset = onRestorePreset,
                modifier = trailingModifier,
            )
        },
    )
}

@Preview
@Composable
private fun QuickAddRowPreview() {
    DrinkWaterAppTheme {
        QuickAddRow(
            quickPresets = drinkSuggestionsMocks.take(3),
            allPresets = drinkSuggestionsMocks,
            onDrink = {},
            onCreatePreset = {},
            onReorder = { _, _ -> },
            onDelete = {},
            onRestorePreset = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
