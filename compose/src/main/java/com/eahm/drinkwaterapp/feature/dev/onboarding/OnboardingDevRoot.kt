package com.eahm.drinkwaterapp.feature.dev.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingDevRoot(
    onBackClicked: () -> Unit,
    viewModel: OnboardingDevViewModel = koinViewModel(),
) {
    var showOnboarding by remember { mutableStateOf(false) }

    if (showOnboarding) {
        BackHandler { showOnboarding = false }
        OnboardingScreen(onComplete = { showOnboarding = false })
        return
    }

    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        OnboardingDevState.Loading -> {}
        is OnboardingDevState.Data -> {
            OnboardingDevScreen(
                isCompleted = data.isCompleted,
                onReset = viewModel::resetOnboarding,
                onStartOnboarding = { showOnboarding = true },
                onBackClicked = onBackClicked,
            )
        }
    }
}
