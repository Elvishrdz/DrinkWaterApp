package com.eahm.drinkwaterapp.feature.dev.backup

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun BackupDevRoot(
    onBackClicked: () -> Unit,
) {
    val viewModel = koinViewModel<BackupDevViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClicked)

    BackupDevScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClicked = onBackClicked,
    )
}
