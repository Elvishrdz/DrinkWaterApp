package com.eahm.drinkwaterapp.feature.dev.dailygoal

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DailyGoalDevRoot(
    onBackClicked: () -> Unit,
    viewModel: DailyGoalDevViewModel = koinViewModel(),
) {
    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        DailyGoalDevState.Loading -> {
            /* Idle */
        }

        is DailyGoalDevState.Data -> {
            DailyGoalDevScreen(
                state = data,
                onInputGoalChanged = viewModel::onInputGoalChanged,
                onSetTodayGoal = viewModel::setGoalFromToday,
                onSelectedDateChanged = viewModel::onSelectedDateChanged,
                onSetGoalForSelectedDay = viewModel::setGoalForSelectedDay,
                onClearAll = viewModel::clearAllRecords,
                onBackClicked = onBackClicked,
            )
        }
    }
}
