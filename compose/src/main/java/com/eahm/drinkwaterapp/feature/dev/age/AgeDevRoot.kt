package com.eahm.drinkwaterapp.feature.dev.age

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.age.AgeCollectionRoot
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AgeDevRoot(
    onBackClicked: () -> Unit,
) {
    val viewModel = koinViewModel<AgeDevViewModel>()
    var showAgeScreen by remember { mutableStateOf(false) }

    if (showAgeScreen) {
        BackHandler { showAgeScreen = false }
        AgeCollectionRoot(
            onComplete = {
                showAgeScreen = false
                viewModel.refresh()
            },
            viewModel = koinViewModel(),
        )
        return
    }

    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    AgeDevScreen(
        state = state,
        onSetOverrideEnabled = viewModel::setOverrideEnabled,
        onSelectResponse = viewModel::selectResponse,
        onRefresh = viewModel::refresh,
        onClearStored = viewModel::clearStoredAge,
        onSubmitBirthYear = viewModel::submitBirthYear,
        onForceShowAgeScreen = { showAgeScreen = true },
        onBackClicked = onBackClicked,
    )
}
