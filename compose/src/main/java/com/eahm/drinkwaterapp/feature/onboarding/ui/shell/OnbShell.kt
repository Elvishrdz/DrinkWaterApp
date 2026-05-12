package com.eahm.drinkwaterapp.feature.onboarding.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons

/**
 * Provides the animated progress fraction (0f–1f) to [OnbTopAppBar].
 * Supplied by [OnboardingScreen] so the animation persists across step changes.
 * Falls back to a static calculation when `null` (previews).
 */
internal val LocalOnboardingProgress = compositionLocalOf<Float?> { null }

private val ArtCircleSize = 72.dp
private val BodyCornerRadius = 32.dp

/**
 * Shared onboarding shell used by all 11 steps.
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
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GradientBackground()
        OnbBubbles()

        Column {
            OnbTopAppBar(
                onBack = onBack,
                step = step,
                total = total,
                modifier = modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth),
                shape = RoundedCornerShape(topStart = BodyCornerRadius, topEnd = BodyCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                ) {
                    // header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        art?.let { content ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Top)
                                    .padding(end = 7.dp),
                                contentAlignment = Alignment.Center,
                                content = { content() },
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (subtitle != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
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
}

@Composable
private fun OnbTopAppBar(
    step: Int,
    total: Int,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // Use the animated value provided by OnboardingScreen (persists across step changes).
    // Fall back to a static fraction in previews where no provider exists.
    val progress = LocalOnboardingProgress.current ?: (step.toFloat() / total.toFloat())

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = OnbIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "$step of $total",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Spacer(Modifier.height(7.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(horizontal = 8.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.30f),
        )
    }
}

@Composable
private fun GradientBackground() {
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
private fun OnbBubbles() {
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
private fun OnbShellPreview() {
    DrinkWaterAppTheme {
        OnbShell(
            step = 3,
            total = OnboardingSteps.totalSteps,
            title = "What's your name?",
            subtitle = "We'll use it to greet you on the home screen.",
            art = { OnbArtCircle(icon = OnbIcons.Person) },
            onBack = {},
            footer = {
                DwPrimaryButton(
                    text = "Continue",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) { /* content placeholder */ }
    }
}
