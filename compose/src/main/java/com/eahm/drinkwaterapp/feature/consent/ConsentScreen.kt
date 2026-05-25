package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.runtime.Composable
import com.eahm.drinkwaterapp.feature.consent.legal.LegalScreen
import com.eahm.drinkwaterapp.feature.consent.trackingrequest.TrackingRequestScreen

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
                ConsentStep.LEGAL -> LegalScreen(
                    privacyToggleChecked = state.privacyToggleChecked,
                    showStepCounter = state.showStepCounter,
                    showNewBadge = state.showNewBadge,
                    onToggleChanged = { onAction(ConsentAction.SetPrivacyToggle(it)) },
                    onAccept = { onAction(ConsentAction.AcceptPrivacyPolicy) },
                )

                ConsentStep.TRACKING_REQUEST -> TrackingRequestScreen(
                    isTrackingEnabled = state.isTrackingEnabled,
                    showStepCounter = state.showStepCounter,
                    onAllow = { onAction(ConsentAction.AllowTracking) },
                    onDecline = { onAction(ConsentAction.DeclineTracking) },
                )
            }
        }
    }
}
