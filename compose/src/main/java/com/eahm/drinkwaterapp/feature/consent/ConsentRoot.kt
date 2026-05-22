package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.consent.ui.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConsentRoot(
    onConsentCompleted: () -> Unit,
) {
    val viewModel = koinViewModel<ConsentViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ConsentEvent.ConsentCompleted -> onConsentCompleted()
        }
    }

    ConsentScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
