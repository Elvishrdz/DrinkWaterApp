package com.eahm.drinkwaterapp.feature.dev.streak

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StreakDevRoot(
    onBackClicked: () -> Unit,
    viewModel: StreakDevViewModel = koinViewModel(),
) {
    BackHandler(onBack = onBackClicked)
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        StreakDevState.Loading -> {
            /* Idle */
        }

        is StreakDevState.Data -> {
            StreakDevScreen(
                state = data,
                onSelectedDateChanged = viewModel::onSelectedDateChanged,
                onDrinkAmountChanged = viewModel::onDrinkAmountChanged,
                onAddDrink = viewModel::addDrinkForSelectedDate,
                onClearHistoryForDate = viewModel::clearHistoryForSelectedDate,
                onClearAllHistory = viewModel::clearAllHistory,
                onSetStoredBestGoalStreak = viewModel::setStoredBestGoalStreak,
                onSetStoredBestDrinkStreak = viewModel::setStoredBestDrinkStreak,
                onClearStoredBests = viewModel::clearStoredBests,
                onBackClicked = onBackClicked,
            )
        }
    }
}
