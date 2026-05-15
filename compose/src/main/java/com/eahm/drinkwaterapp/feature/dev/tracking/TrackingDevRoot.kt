package com.eahm.drinkwaterapp.feature.dev.tracking

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TrackingDevRoot(onBackClicked: () -> Unit) {
    val viewModel: TrackingDevViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClicked)

    TrackingDevScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
