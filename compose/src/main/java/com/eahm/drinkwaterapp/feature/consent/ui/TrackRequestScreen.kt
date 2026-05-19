package com.eahm.drinkwaterapp.feature.consent.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

private val TrackWaveHeight = 220.dp
private val TrackOverlapHeight = 25.dp

@Composable
internal fun TrackRequestScreen(
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val innerPadding = PaddingValues()
    when (getLayoutType()) {
        LayoutType.PORTRAIT -> TrackPortraitLayout(
            innerPadding = innerPadding,
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_EXPANDED -> TrackLandscapeLayout(
            innerPadding = innerPadding,
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.TABLE_TOP -> TrackTableTopLayout(
            innerPadding = innerPadding,
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )
    }
}

// region Layouts

/**
 * Portrait: wave header at the top, overlapping white sheet at the bottom.
 * Identical to the original single-column design.
 */
@Composable
private fun TrackPortraitLayout(
    innerPadding: PaddingValues,
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        val sheetHeight = maxHeight - (TrackWaveHeight - TrackOverlapHeight)

        // Wave header
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(TrackWaveHeight)
                .zIndex(0f)
                .background(magentaGradient),
        ) {
            TrackBubbles()
            Column(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 40.dp),
            ) {
                TrackHeaderContent(showStepCounter = showStepCounter, centered = false)
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
                TrackMainContent()
                Spacer(Modifier.weight(1f))
                TrackButtons(
                    isTrackingEnabled = isTrackingEnabled,
                    onAllow = onAllow,
                    onDecline = onDecline
                )
            }
        }
    }
}

/**
 * Landscape / foldable-expanded: gradient branding panel on the left,
 * white content surface on the right.
 */
@Composable
private fun TrackLandscapeLayout(
    innerPadding: PaddingValues,
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Left: gradient branding panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(magentaGradient),
        ) {
            TrackBubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TrackHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        // Right: white surface — scrollable content area + buttons pinned at bottom
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TrackMainContent()
                }
                Spacer(Modifier.height(14.dp))
                TrackButtons(
                    isTrackingEnabled = isTrackingEnabled,
                    onAllow = onAllow,
                    onDecline = onDecline
                )
            }
        }
    }
}

/**
 * Table-top: gradient branding panel on the top half,
 * white content surface on the bottom half.
 */
@Composable
private fun TrackTableTopLayout(
    innerPadding: PaddingValues,
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    val magentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // Top: gradient branding panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(magentaGradient),
        ) {
            TrackBubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TrackHeaderContent(showStepCounter = showStepCounter, centered = true)
            }
        }

        // Bottom: white surface — scrollable content area + buttons pinned at bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TrackMainContent()
                }
                Spacer(Modifier.height(14.dp))
                TrackButtons(
                    isTrackingEnabled = isTrackingEnabled,
                    onAllow = onAllow,
                    onDecline = onDecline
                )
            }
        }
    }
}

// endregion

// region Shared composables

@Composable
private fun TrackBubbles() {
    Box(modifier = Modifier.fillMaxSize()) {
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
    }
}

@Composable
private fun TrackHeaderContent(
    showStepCounter: Boolean,
    centered: Boolean,
) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start

    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.30f),
                RoundedCornerShape(20.dp),
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
            text = stringResource(R.string.consent_track_step),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.14.em,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
    } else {
        Spacer(Modifier.height(16.dp))
    }

    Text(
        text = stringResource(R.string.consent_track_title),
        color = Color.White,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Lede text + feature card. Shared across all layout variants. */
@Composable
private fun TrackMainContent() {
    Text(
        text = stringResource(R.string.consent_track_lede),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

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
                title = stringResource(R.string.consent_track_feature_usage_title),
                description = stringResource(R.string.consent_track_feature_usage_desc),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            FeatureCardRow(
                imageVector = DwIcons.Star,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                title = stringResource(R.string.consent_track_feature_private_title),
                description = stringResource(R.string.consent_track_feature_private_desc),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            FeatureCardRow(
                imageVector = DwIcons.Settings,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                title = stringResource(R.string.consent_track_feature_control_title),
                description = stringResource(R.string.consent_track_feature_control_desc),
            )
        }
    }
}

/** Accept / decline buttons. Shared across all layout variants. */
@Composable
private fun TrackButtons(
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

// endregion

// region Internal composables

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

// endregion

// region Previews

@Preview(name = "Portrait")
@Composable
private fun TrackRequestScreenPortraitPreview() {
    DrinkWaterAppTheme {
        TrackRequestScreen(
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun TrackRequestScreenLandscapePreview() {
    DrinkWaterAppTheme {
        TrackLandscapeLayout(
            innerPadding = PaddingValues(0.dp),
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}

@Preview(name = "TableTop", widthDp = 700, heightDp = 700)
@Composable
private fun TrackRequestScreenTableTopPreview() {
    DrinkWaterAppTheme {
        TrackTableTopLayout(
            innerPadding = PaddingValues(0.dp),
            isTrackingEnabled = false,
            showStepCounter = true,
            onAllow = {},
            onDecline = {},
        )
    }
}

// endregion
