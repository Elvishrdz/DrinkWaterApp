package com.eahm.drinkwaterapp.feature.dev.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NotificationsRoot(
    onBackClicked: () -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { /* handled as feature grows */ }
    }

    BackHandler(onBack = onBackClicked)

    NotificationsScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
