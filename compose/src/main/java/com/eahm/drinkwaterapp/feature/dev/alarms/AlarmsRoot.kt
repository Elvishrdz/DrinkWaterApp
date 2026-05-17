package com.eahm.drinkwaterapp.feature.dev.alarms

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
internal fun AlarmsRoot(onBackClicked: () -> Unit) {
    val viewModel: AlarmsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when (it) {
                AlarmsEvent.OnResumeCalled -> snackbarHostState.showSnackbar("On resume call completed")
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(AlarmsAction.RefreshPermissionState)
    }

    BackHandler(onBack = onBackClicked)

    AlarmsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
