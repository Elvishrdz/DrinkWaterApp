package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

// Ocean-variant hero gradient, matching GoalCelebrationCard "Ocean" theme
private val HeroTop = Color(0xFF4FB3DD)
private val HeroBottom = Color(0xFF0E5A82)

/** Step 7 — Overlay / "display over other apps" permission. */
@Composable
internal fun StepOverlay(
    step: Int,
    overlayGranted: Boolean,
    onAllow: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    if (overlayGranted) {
        GrantedLayout(step = step, onContinue = onContinue, onBack = onBack)
    } else {
        RequestLayout(step = step, onAllow = onAllow, onSkip = onContinue, onBack = onBack)
    }
}

// ── Not granted ───────────────────────────────────────────────────────────────

@Composable
private fun RequestLayout(
    step: Int,
    onAllow: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_overlay_request_title),
        subtitle = stringResource(R.string.onb_overlay_request_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Star) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.onb_overlay_request_cta_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            DwPrimaryButton(
                text = stringResource(R.string.overlay_allow_over_other_apps),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        CelebrationCardPreview()
        Spacer(Modifier.height(16.dp))
        OverlayStepsList()
    }
}

// ── Already granted ───────────────────────────────────────────────────────────

@Composable
private fun GrantedLayout(
    step: Int,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_overlay_granted_title),
        subtitle = stringResource(R.string.onb_overlay_granted_subtitle),
        art = { OnbArtCircle(icon = OnbIcons.Check) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        CelebrationCardPreview()
        Spacer(Modifier.height(16.dp))
        GrantedFeatureList()
    }
}

// ── Shared: celebration card preview ─────────────────────────────────────────

/**
 * A static mockup of the GoalCelebrationCard so the user can see what they'll get.
 * Uses the Ocean variant gradient (DwWater family) to match the real widget.
 */
@Composable
private fun CelebrationCardPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Brush.verticalGradient(listOf(HeroTop, HeroBottom)))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Medallion
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DwIcons.Drop,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = stringResource(R.string.onb_overlay_card_preview_title),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.onb_overlay_card_preview_body),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.80f),
        )

        // Stats strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(Color.White.copy(alpha = 0.12f))
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatCell(
                value = "2.0 L",
                label = stringResource(R.string.onb_overlay_card_preview_stat_goal)
            )
            StatCell(
                value = "8",
                label = stringResource(R.string.onb_overlay_card_preview_stat_drinks)
            )

            if (FeatureFlags.streakFeatureEnabled) {
                StatCell(
                    value = "3",
                    label = stringResource(R.string.onb_overlay_card_preview_stat_streak)
                )
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = DwMagenta,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.70f),
        )
    }
}

// ── Request: how-to steps ─────────────────────────────────────────────────────

@Composable
private fun OverlayStepsList() {
    val steps = listOf(
        stringResource(R.string.onb_overlay_step_1),
        stringResource(R.string.onb_overlay_step_2, stringResource(R.string.app_name)),
        stringResource(R.string.onb_overlay_step_3),
        stringResource(R.string.onb_overlay_step_4),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.onb_overlay_steps_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        steps.forEachIndexed { index, text ->
            StepRow(number = index + 1, text = text, isLast = index == steps.lastIndex)
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String, isLast: Boolean) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isLast) DwTintMagenta else DwTintWater),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isLast) DwMagenta else DwWater,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Granted: feature explanation rows ────────────────────────────────────────

@Composable
private fun GrantedFeatureList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        FeatureRow(
            icon = DwIcons.Drop,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.onb_overlay_feature_app_label),
            description = stringResource(R.string.onb_overlay_feature_app_description),
        )
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(Modifier.height(12.dp))
        FeatureRow(
            icon = DwIcons.Bell,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = stringResource(R.string.onb_overlay_feature_reminder_label),
            description = stringResource(R.string.onb_overlay_feature_reminder_description),
        )
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@DwPreview
@Composable
private fun StepOverlayRequestPreview() {
    DrinkWaterAppTheme {
        StepOverlay(
            step = 7,
            overlayGranted = false,
            onAllow = {},
            onContinue = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepOverlayGrantedPreview() {
    DrinkWaterAppTheme {
        StepOverlay(
            step = 7,
            overlayGranted = true,
            onAllow = {},
            onContinue = {},
            onBack = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepOverlayLandscapePreview() {
    DrinkWaterAppTheme {
        StepOverlay(step = 8, overlayGranted = false, onAllow = {}, onContinue = {}, onBack = {})
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepOverlayTabletPreview() {
    DrinkWaterAppTheme {
        StepOverlay(step = 8, overlayGranted = false, onAllow = {}, onContinue = {}, onBack = {})
    }
}
