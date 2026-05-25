package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion

internal sealed interface DrinkAction {

    data class Drink(
        val amount: Float,
        val source: DrinkSource,
    ) : DrinkAction

    data class ReorderPresets(
        val newOrder: List<DrinkSuggestion>,
        val reorderingQuickPresets: Boolean,
    ) : DrinkAction

    data class CreatePreset(val amount: Float) : DrinkAction
    data class DeletePreset(val preset: DrinkSuggestion) : DrinkAction
    data class RestorePreset(val preset: DrinkSuggestion) : DrinkAction
    data class BottomSheetOpened(val presetCount: Int) : DrinkAction
}
