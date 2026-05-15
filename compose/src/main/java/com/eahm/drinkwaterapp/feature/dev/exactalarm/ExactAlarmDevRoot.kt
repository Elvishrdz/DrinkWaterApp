package com.eahm.drinkwaterapp.feature.dev.exactalarm

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ExactAlarmDevRoot(
    onBackClicked: () -> Unit,
    viewModel: ExactAlarmDevViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Refresh permission status every time this screen resumes
    // (user may have come back from system Settings)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(ExactAlarmDevAction.RefreshPermissionState)
    }

    BackHandler(onBack = onBackClicked)

    ExactAlarmDevScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
