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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
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

/** Step 7 — Schedule exact alarm permission. */
@Composable
internal fun StepExactAlarm(
    step: Int,
    exactAlarmGranted: Boolean,
    onAllow: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    if (exactAlarmGranted) {
        GrantedLayout(step = step, onContinue = onContinue, onBack = onBack)
    } else {
        RequestLayout(step = step, onAllow = onAllow, onSkip = onSkip, onBack = onBack)
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
        title = stringResource(R.string.onb_exact_alarm_title),
        subtitle = stringResource(R.string.onb_exact_alarm_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Clock) },
        onBack = onBack,
        footer = {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.onb_exact_alarm_cta_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            DwPrimaryButton(
                text = stringResource(R.string.onb_exact_alarm_cta_allow),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        BenefitList()
        Spacer(Modifier.height(16.dp))
        HowToStepsList()
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
        title = stringResource(R.string.onb_exact_alarm_granted_title),
        subtitle = stringResource(R.string.onb_exact_alarm_granted_subtitle),
        art = { OnbArtCircle(icon = OnbIcons.Check) },
        onBack = onBack,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        BenefitList()
    }
}

// ── Shared: benefit list ──────────────────────────────────────────────────────

@Composable
private fun BenefitList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        BenefitRow(
            icon = DwIcons.Clock,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.onb_exact_alarm_benefit_timing_label),
            description = stringResource(R.string.onb_exact_alarm_benefit_timing_description),
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))
        BenefitRow(
            icon = DwIcons.ShieldCheck,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = stringResource(R.string.onb_exact_alarm_benefit_reliable_label),
            description = stringResource(R.string.onb_exact_alarm_benefit_reliable_description),
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))
        BenefitRow(
            icon = DwIcons.Drop,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.onb_exact_alarm_benefit_habit_label),
            description = stringResource(R.string.onb_exact_alarm_benefit_habit_description),
        )
    }
}

@Composable
private fun BenefitRow(
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

// ── Request: how-to steps ─────────────────────────────────────────────────────

@Composable
private fun HowToStepsList() {
    val steps = listOf(
        stringResource(R.string.onb_exact_alarm_step_1),
        stringResource(R.string.onb_exact_alarm_step_2),
        stringResource(R.string.onb_exact_alarm_step_3),
        stringResource(R.string.onb_exact_alarm_step_4),
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
            text = stringResource(R.string.onb_exact_alarm_steps_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        steps.forEachIndexed { index, text ->
            HowToStepRow(number = index + 1, text = text, isLast = index == steps.lastIndex)
        }
    }
}

@Composable
private fun HowToStepRow(number: Int, text: String, isLast: Boolean) {
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

// ── Previews ──────────────────────────────────────────────────────────────────

@DwPreview
@Composable
private fun StepExactAlarmRequestPreview() {
    DrinkWaterAppTheme {
        StepExactAlarm(
            step = 7,
            exactAlarmGranted = false,
            onAllow = {},
            onContinue = {},
            onSkip = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepExactAlarmGrantedPreview() {
    DrinkWaterAppTheme {
        StepExactAlarm(
            step = 7,
            exactAlarmGranted = true,
            onAllow = {},
            onContinue = {},
            onSkip = {},
            onBack = {},
        )
    }
}
