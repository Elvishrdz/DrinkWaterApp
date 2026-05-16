package com.eahm.drinkwaterapp.feature.consent.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark

private val TrackWaveHeight = 220.dp
private val TrackOverlapHeight = 22.dp

@Composable
internal fun TrackRequestScreen(
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

    Scaffold(modifier = modifier) { innerPadding ->
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val sheetHeight = maxHeight - (TrackWaveHeight - TrackOverlapHeight)

            // Wave header
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(TrackWaveHeight)
                    .zIndex(0f)
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(magentaGradient),
            ) {
                // Decorative bubbles
                Box(
                    Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.14f), CircleShape),
                )
                Box(
                    Modifier
                        .size(90.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 8.dp, y = 60.dp)
                        .background(Color.White.copy(alpha = 0.09f), CircleShape),
                )
                Box(
                    Modifier
                        .size(60.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-60).dp, y = 10.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape),
                )

                // Wave content
                Column(
                    Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 40.dp),
                ) {
                    // Icon in frosted rounded square
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.30f),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = DwIcons.PulseHeart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (showStepCounter) {
                        // Step 1 = done, step 2 = active
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.65f)),
                            )
                            Box(
                                Modifier
                                    .width(30.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Step 2 of 2",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.14.em,
                        )
                        Spacer(Modifier.height(6.dp))
                    } else {
                        Spacer(Modifier.height(16.dp))
                    }

                    Text(
                        text = "Help shape the app",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Overlapping white sheet
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .align(Alignment.BottomStart)
                    .zIndex(1f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Lede
                    Text(
                        text = "Share anonymous usage so we can see which features people love and which need work. Metrics only, no name, no email from you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Feature card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column {
                            FeatureCardRow(
                                imageVector = DwIcons.Lightbulb,
                                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                title = "Feature usage",
                                description = "We want to know what works and what not from a general view.",
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            FeatureCardRow(
                                imageVector = DwIcons.Star,
                                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                title = "Never linked to you",
                                description = "We use a random generated id that is NOT linked to any of your information.",
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            FeatureCardRow(
                                imageVector = DwIcons.Settings,
                                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                title = "Off any time",
                                description = "Disable it anytime under Settings → Privacy → Usage data",
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Buttons
                    AnimatedContent(targetState = isTrackingEnabled) { tracking ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (tracking) {
                                Text(
                                    text = "Tracking is active. You can disable it in Settings → Privacy.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(8.dp))
                                DwPrimaryButton(
                                    text = "Continue",
                                    onClick = onAllow,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                DwPrimaryButton(
                                    text = "Share anonymous data",
                                    onClick = onAllow,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                DwTertiaryButton(
                                    text = "Not now",
                                    onClick = onDecline,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCardRow(
    imageVector: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
