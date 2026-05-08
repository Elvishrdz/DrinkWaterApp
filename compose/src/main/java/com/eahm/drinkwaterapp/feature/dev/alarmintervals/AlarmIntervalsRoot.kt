package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AlarmIntervalsRoot(onBackClicked: () -> Unit) {
    val viewModel: AlarmIntervalsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { /* handled as feature grows */ }
    }

    BackHandler(onBack = onBackClicked)

    AlarmIntervalsScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
