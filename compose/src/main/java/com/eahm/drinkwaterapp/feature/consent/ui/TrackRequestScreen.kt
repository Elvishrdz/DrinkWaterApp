package com.eahm.drinkwaterapp.feature.consent.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.consent.FeatureRow

@Composable
internal fun TrackRequestScreen(
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = DwIcons.Wave,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )

            if (showStepCounter) {
                Text(text = "Step 2 of 2")
            }

            Text(
                text = "Help shape the app",
                fontWeight = FontWeight.Bold,
            )

            Text(text = "Share anonymous usage so we can see which features people love and which need work. Metrics only, no name, no email from you.")

            FeatureRow(
                icon = {
                    Icon(
                        imageVector = DwIcons.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Feature usage",
                description = "We want to know what works and what not from a general view.",
            )

            FeatureRow(
                icon = {
                    Icon(
                        imageVector = DwIcons.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Never linked to you",
                description = "We use a random generated id that is NOT linked to any of your information.",
            )

            FeatureRow(
                icon = {
                    Icon(
                        imageVector = DwIcons.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Off any time",
                description = "Disable it anytime under Settings → Privacy → Usage data",
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = isTrackingEnabled,
            ) { state ->
                Column {
                    if (state) {
                        Text(text = "Tracking is active, you can disable it in app settings")
                        DwPrimaryButton(
                            text = "Continue",
                            onClick = onAllow,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        DwTertiaryButton(
                            text = "Not now",
                            onClick = onDecline,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DwPrimaryButton(
                            text = "Allow sharing usage data",
                            onClick = onAllow,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TrackRequestScreenPreview() {
    DrinkWaterAppTheme {
        TrackRequestScreen(
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}
