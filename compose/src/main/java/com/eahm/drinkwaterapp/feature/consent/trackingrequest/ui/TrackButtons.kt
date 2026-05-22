package com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

/** Accept / decline buttons. Shared across all layout variants. */
@Composable
internal fun TrackButtons(
    isTrackingEnabled: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    AnimatedContent(targetState = isTrackingEnabled) { tracking ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (tracking) {
                Text(
                    text = stringResource(R.string.consent_track_active),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                DwPrimaryButton(
                    text = stringResource(R.string.onb_cta_continue),
                    onClick = onAllow,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DwTertiaryButton(
                    text = stringResource(R.string.action_not_now),
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth(),
                )
                DwPrimaryButton(
                    text = stringResource(R.string.consent_track_cta_allow),
                    onClick = onAllow,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private class TrackingEnabledPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TrackButtonsPreview(
    @PreviewParameter(TrackingEnabledPreviewProvider::class) isTrackingEnabled: Boolean,
) {
    DrinkWaterAppTheme {
        Column(Modifier.padding(16.dp)) {
            TrackButtons(
                isTrackingEnabled = isTrackingEnabled,
                onAllow = {},
                onDecline = {},
            )
        }
    }
}
