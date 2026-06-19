package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
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
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.SkipButton
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

private val HeroTop = Color(0xFF4FB3DD)
private val HeroBottom = Color(0xFF0E5A82)

@Composable
internal fun stepOverlay(
    onContinue: () -> Unit,
    onBack: () -> Unit,
): OnboardingStep {
    val context = LocalContext.current
    // Overlay ("display over other apps") permission launcher.
    // Auto-advances when the user grants permission and returns to the app.
    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val permissionGranted = Settings.canDrawOverlays(context)
        if (permissionGranted) {
            onContinue()
        }
    }

    return if (Settings.canDrawOverlays(context)) {
        stepOverlayGranted(onBack = onBack, onContinue = onContinue)

    } else {
        stepRequestOverlay(
            onBack = onBack,
            onContinue = onContinue,
            requestOverlayPermission = {
                overlayLauncher.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri(),
                    )
                )
            }
        )
    }
}

@Composable
private fun stepOverlayGranted(
    onBack: () -> Unit,
    onContinue: () -> Unit
) = OnboardingStep(
    art = { OnbArtCircle(icon = OnbIcons.Check) },
    title = stringResource(R.string.onb_overlay_granted_title),
    subtitle = stringResource(R.string.onb_overlay_granted_subtitle),
    onBack = onBack,
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.onb_cta_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        CelebrationCardPreview()
        Spacer(Modifier.height(16.dp))
        GrantedFeatureList()
    },
)

@Composable
private fun stepRequestOverlay(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    requestOverlayPermission: () -> Unit
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Star) },
    title = stringResource(R.string.onb_overlay_request_title),
    subtitle = stringResource(R.string.onb_overlay_request_subtitle),
    onBack = onBack,
    footer = {
        SkipButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        DwPrimaryButton(
            text = stringResource(R.string.overlay_allow_over_other_apps),
            onClick = requestOverlayPermission,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        CelebrationCardPreview()
        Spacer(Modifier.height(16.dp))
        OverlayStepsList()
    },
)

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
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DwMagenta)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.70f)
        )
    }
}

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
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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

@Composable
private fun StepOverlayBasePreview(overlayGranted: Boolean) {
    OnboardingStepBasePreview(
        config = if (overlayGranted) {
            stepOverlayGranted(
                onBack = {},
                onContinue = {},
            )
        } else {
            stepRequestOverlay(
                onBack = {},
                onContinue = {},
                requestOverlayPermission = {},
            )
        }
    )
}

@DwPreview
@Composable
private fun StepOverlayPreview() {
    DrinkWaterAppTheme {
        StepOverlayBasePreview(overlayGranted = true)
    }
}

@DwPreview
@Composable
private fun StepOverlayGrantedPreview() {
    DrinkWaterAppTheme {
        StepOverlayBasePreview(overlayGranted = false)
    }
}
