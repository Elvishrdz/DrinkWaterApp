package com.eahm.drinkwaterapp.feature.onboarding.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui.OnbTopAppBar

/**
 * Provides the animated progress fraction (0f–1f) to [OnbTopAppBar].
 * Supplied by [OnboardingScreen] so the animation persists across step changes.
 * Falls back to a static calculation when `null` (previews).
 */
internal val LocalOnboardingProgress = compositionLocalOf<Float?> { null }

internal val ArtCircleSize = 72.dp
internal val BodyCornerRadius = 30.dp

/**
 * Shared onboarding shell used by all 11 steps.
 *
 * When [useAdaptiveLayout] is `true`, the shell switches between portrait,
 * landscape, and table-top arrangements based on the current [LayoutType].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnbShell(
    step: Int,
    total: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    art: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    useAdaptiveLayout: Boolean = true,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    if (useAdaptiveLayout) {
        when (getLayoutType()) {
            LayoutType.LANDSCAPE, LayoutType.FOLDABLE_EXPANDED -> OnbShellLandscape(
                step = step, total = total, title = title, subtitle = subtitle,
                art = art, onBack = onBack, modifier = modifier, footer = footer, content = content,
            )

            LayoutType.TABLE_TOP -> OnbShellTableTop(
                step = step, total = total, title = title, subtitle = subtitle,
                art = art, onBack = onBack, modifier = modifier, footer = footer, content = content,
            )

            LayoutType.PORTRAIT -> OnbShellPortrait(
                step = step, total = total, title = title, subtitle = subtitle,
                art = art, onBack = onBack, modifier = modifier, footer = footer,
                scrollableContent = content,
            )
        }
    } else {
        OnbShellPortrait(
            step = step, total = total, title = title, subtitle = subtitle,
            art = art, onBack = onBack, modifier = modifier, footer = footer,
            scrollableContent = content,
        )
    }
}


/**
 * Table-top layout.
 * Gradient panel on the top half (top bar + header art/text), white surface panel on the bottom half (content + footer).
 */
@Composable
private fun OnbShellTableTop(
    step: Int,
    total: Int,
    title: String,
    subtitle: String?,
    art: (@Composable () -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top: gradient branding panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            GradientBackground()
            OnbBubbles()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                OnbTopAppBar(
                    step = step,
                    total = total,
                    onBack = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        art?.let { artContent ->
                            Box(
                                modifier = Modifier.padding(bottom = 12.dp),
                                contentAlignment = Alignment.Center,
                                content = { artContent() },
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        if (subtitle != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.80f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        // Bottom: white surface content panel
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = BodyCornerRadius, topEnd = BodyCornerRadius),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                ) {
                    content()
                }
                Spacer(Modifier.height(12.dp))
                footer()
            }
        }
    }
}

@Composable
internal fun GradientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to DwWaterLight,
                        0.3f to DwWater,
                        0.6f to DwWaterDeep,
                    )
                )
            ),
    )
}

@Composable
internal fun OnbBubbles() {
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-70).dp, y = (-70).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.TopEnd)
                .offset(x = 45.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f)),
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 15.dp, y = (-15).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f)),
        )
    }
}

@Composable
internal fun OnbArtCircle(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ArtCircleSize)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(DwWater, DwWaterDeep))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
    }
}

@DwPreview
@Composable
private fun OnbShellTableTopPreview() {
    DrinkWaterAppTheme {
        OnbShellTableTop(
            step = 7,
            total = OnboardingSteps.totalSteps,
            title = "Alarms that arrive on time",
            subtitle = "Allow the app to schedule precise alarms so your reminders fire exactly when you expect them.",
            art = { OnbArtCircle(icon = OnbIcons.Check) },
            onBack = {},
            footer = {
                DwPrimaryButton(
                    text = "Allow exact alarms",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) { /* content placeholder */ }
    }
}
