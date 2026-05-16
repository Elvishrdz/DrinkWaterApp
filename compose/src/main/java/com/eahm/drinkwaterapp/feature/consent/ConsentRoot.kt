package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConsentRoot(
    onConsentCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ConsentViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ConsentEvent.ConsentCompleted -> onConsentCompleted()
            }
        }
    }

    ConsentScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
