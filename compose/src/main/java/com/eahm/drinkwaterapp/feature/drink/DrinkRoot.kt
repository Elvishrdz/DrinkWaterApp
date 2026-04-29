package com.eahm.drinkwaterapp.feature.drink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.drink.ui.DrinkScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DrinkRoot(
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
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
                onStopAlarm = viewModel::onStopAlarm,
                onGoalAmountChanged = viewModel::onGoalChanged,
                getRemainingAlarmTime = viewModel::getRemainingAlarmTime,
                onAlarmIntervalChange = viewModel::onAlarmIntervalChange,
            )
        }
    }
}
