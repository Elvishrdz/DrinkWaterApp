package com.eahm.drinkwaterapp.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HistoryRoot() {
    val viewModel: HistoryViewModel = koinViewModel()
    val purchaseManager = koinInject<PurchaseManager>()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isAdFree by purchaseManager.isAdFree.collectAsStateWithLifecycle()

    when (val data = state) {
        HistoryState.Loading -> {
            /* Idle */
        }

        is HistoryState.Data -> {
            HistoryScreen(
                state = data,
                isAdFree = isAdFree,
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
                onGoToToday = viewModel::goToToday,
            )
        }
    }
}
