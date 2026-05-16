package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.feature.consent.ui.PrivacyPolicyScreen
import com.eahm.drinkwaterapp.feature.consent.ui.TrackRequestScreen

internal enum class ConsentStep { PRIVACY_POLICY, TRACKING }

@Composable
internal fun ConsentScreen(
    state: ConsentState,
    onAction: (ConsentAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ConsentState.Loading -> {
            // idle
        }

        is ConsentState.Data -> {
            AnimatedContent(
                targetState = state.step,
                modifier = modifier,
            ) { step ->
                when (step) {
                    ConsentStep.PRIVACY_POLICY -> PrivacyPolicyScreen(
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
}

@Composable
internal fun FeatureRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = description)
        }
    }
}
