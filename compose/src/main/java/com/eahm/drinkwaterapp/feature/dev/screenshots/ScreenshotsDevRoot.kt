package com.eahm.drinkwaterapp.feature.dev.screenshots

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ScreenshotsDevRoot(
    onBackClicked: () -> Unit,
    viewModel: ScreenshotsDevViewModel = koinViewModel(),
) {
    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        ScreenshotsDevState.Loading -> {
            /* Idle */
        }

        is ScreenshotsDevState.Data -> {
            ScreenshotsDevScreen(
                state = data,
                onVersionChanged = viewModel::onVersionChanged,
                onSetForScreenshots = viewModel::setForScreenshots,
                onInsertPreset = viewModel::insertPreset,
                onClearAll = viewModel::clearAll,
                onBackClicked = onBackClicked,
            )
        }
    }
}
