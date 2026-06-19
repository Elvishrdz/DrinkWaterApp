package com.eahm.drinkwaterapp.feature.dev.stresstest

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StressTestDevRoot(
    onBackClicked: () -> Unit,
) {
    val viewModel = koinViewModel<StressTestDevViewModel>()
    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        StressTestDevState.Loading -> {
            /* Idle */
        }

        is StressTestDevState.Data -> {
            StressTestDevScreen(
                state = data,
                onAnchorDateChanged = viewModel::onAnchorDateChanged,
                onDaysChanged = viewModel::onDaysChanged,
                onGoalModeChanged = viewModel::onGoalModeChanged,
                onDefaultGoalChanged = viewModel::onDefaultGoalChanged,
                onMinGoalChanged = viewModel::onMinGoalChanged,
                onMaxGoalChanged = viewModel::onMaxGoalChanged,
                onChangeEveryDaysChanged = viewModel::onChangeEveryDaysChanged,
                onRangeStartChanged = viewModel::onRangeStartChanged,
                onRangeEndChanged = viewModel::onRangeEndChanged,
                onInsertGenerated = viewModel::insertGenerated,
                onInsertPredefined = viewModel::insertPredefined,
                onInsertPreset = viewModel::insertPreset,
                onClearAll = viewModel::clearAll,
                onBackClicked = onBackClicked,
            )
        }
    }
}
