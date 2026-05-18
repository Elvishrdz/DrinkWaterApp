package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AlarmsV2Root(onBackClicked: () -> Unit) {
    val viewModel: AlarmsV2ViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(AlarmsV2Action.Refresh)
    }

    BackHandler(onBack = onBackClicked)

    AlarmsV2Screen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
