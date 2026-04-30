package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DrinkRoot(
    onNavigateToHistory: () -> Unit,
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
                onDrink = viewModel::onDrink,
                onReorderPresets = viewModel::onPresetsReordered,
                onCreatePreset = viewModel::onCreatePreset,
                onNavigateToHistory = onNavigateToHistory,
            )
        }
    }
}
