package com.eahm.drinkwaterapp.feature.dev.historyinject

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HistoryInjectDevRoot(
    onBackClicked: () -> Unit,
    viewModel: HistoryInjectDevViewModel = koinViewModel(),
) {
    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        HistoryInjectDevState.Loading -> {
            /* Idle */
        }

        is HistoryInjectDevState.Data -> {
            HistoryInjectDevScreen(
                state = data,
                onAddEntry = viewModel::addEntry,
                onRemoveEntry = viewModel::removeEntry,
                onUpdateEntry = viewModel::updateEntry,
                onInsertAll = viewModel::insertAll,
                onClearAll = viewModel::clearAll,
                onSetClearByDay = viewModel::setClearByDay,
                onClearByDay = viewModel::clearByDay,
            )
        }
    }
}
