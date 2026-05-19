package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.runtime.Composable
import com.eahm.drinkwaterapp.feature.consent.ui.LegalScreen
import com.eahm.drinkwaterapp.feature.consent.ui.TrackRequestScreen

internal enum class ConsentStep { PRIVACY_POLICY, TRACKING }

@Composable
internal fun ConsentScreen(
    state: ConsentState,
    onAction: (ConsentAction) -> Unit,
) {
    when (state) {
        ConsentState.Loading -> {
            // idle
        }

        is ConsentState.Data -> {
            when (state.step) {
                ConsentStep.PRIVACY_POLICY -> LegalScreen(
                    privacyToggleChecked = state.privacyToggleChecked,
                    showStepCounter = state.showStepCounter,
                    onToggleChanged = { onAction(ConsentAction.SetPrivacyToggle(it)) },
                    onAccept = { onAction(ConsentAction.AcceptPrivacyPolicy) },
                )

                ConsentStep.TRACKING -> TrackRequestScreen(
                    isTrackingEnabled = state.isTrackingEnabled,
                    showStepCounter = state.showStepCounter,
                    onAllow = { onAction(ConsentAction.AllowTracking) },
                    onDecline = { onAction(ConsentAction.DeclineTracking) },
                )
            }
        }
    }
}
