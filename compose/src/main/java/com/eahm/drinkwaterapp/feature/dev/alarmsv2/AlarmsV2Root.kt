package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AlarmsV2Root(onBackClicked: () -> Unit) {
    val viewModel: AlarmsV2ViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AlarmsV2Event.OnResumeCalled -> snackbarHostState.showSnackbar("On resume call completed")
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(AlarmsV2Action.Refresh)
    }

    BackHandler(onBack = onBackClicked)

    AlarmsV2Screen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
