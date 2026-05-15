package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DrinkRoot(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DrinkViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        DrinkState.Loading -> {
            /* Idle */
        }

        is DrinkState.Data -> {
            DrinkScreen(
                state = data,
                onDrink = { amount, source -> viewModel.onDrink(amount, source) },
                onReorderPresets = viewModel::onPresetsReordered,
                onCreatePreset = viewModel::onCreatePreset,
                onDeletePreset = viewModel::onDeletePreset,
                onRestorePreset = viewModel::onRestorePreset,
                onBottomSheetOpened = viewModel::onBottomSheetOpened,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}
