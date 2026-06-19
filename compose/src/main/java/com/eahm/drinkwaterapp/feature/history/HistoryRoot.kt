package com.eahm.drinkwaterapp.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HistoryRoot() {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        HistoryState.Loading -> {
            /* Idle */
        }

        is HistoryState.Data -> {
            HistoryScreen(
                state = data,
                onDelete = viewModel::onDelete,
                onUpdate = viewModel::onUpdateRecord,
                onDateChanged = viewModel::onDateChanged,
                onGoToToday = viewModel::goToToday,
            )
        }
    }
}
