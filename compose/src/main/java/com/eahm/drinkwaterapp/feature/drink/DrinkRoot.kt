package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.drink.ui.FirstDrinkDialog
import com.eahm.drinkwaterapp.feature.drink.ui.WaterRisingAnimation
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
            Box(Modifier.fillMaxSize()) {
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

                if (data.showFirstDrinkDialog) {
                    FirstDrinkDialog(onDismiss = viewModel::onFirstDrinkDialogDismissed)
                }

                WaterRisingAnimation(
                    trigger = data.drinkAnimationTrigger,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
