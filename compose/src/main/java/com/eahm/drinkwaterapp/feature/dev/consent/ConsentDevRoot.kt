package com.eahm.drinkwaterapp.feature.dev.consent

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.feature.consent.ConsentRoot
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConsentDevRoot(
    onBackClicked: () -> Unit,
    viewModel: ConsentDevViewModel = koinViewModel(),
) {
    var showConsent by remember { mutableStateOf(false) }

    if (showConsent) {
        BackHandler { showConsent = false }
        ConsentRoot(onConsentCompleted = { showConsent = false })
        return
    }

    BackHandler(onBack = onBackClicked)

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        ConsentDevState.Loading -> {}
        is ConsentDevState.Data -> {
            ConsentDevScreen(
                privacyPolicyAcceptedAt = data.privacyPolicyAcceptedAt,
                trackingConsentDecided = data.trackingConsentDecided,
                isTracking = data.isTracking,
                onResetPrivacyPolicy = viewModel::resetPrivacyPolicy,
                onResetTrackingConsentDecided = viewModel::resetTrackingConsentDecided,
                onResetAll = viewModel::resetAll,
                onSimulateReturnAtTrackingStep = viewModel::simulateReturnAtTrackingStep,
                onLaunchConsent = { showConsent = true },
                onBackClicked = onBackClicked,
            )
        }
    }
}
