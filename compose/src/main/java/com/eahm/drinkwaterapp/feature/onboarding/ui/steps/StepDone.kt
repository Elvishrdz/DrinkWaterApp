package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingFormState
import com.eahm.drinkwaterapp.feature.onboarding.TOTAL_STEPS
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

/** Step 11 — Summary / done screen. No art circle; gradient header is minimal. */
@Composable
internal fun StepDone(
    state: OnboardingFormState,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    val displayName = state.name.ifBlank { "friend" }
    val liters = "%.2f".format(state.computedGoalMl / 1000.0)

    OnbShell(
        step = TOTAL_STEPS,
        total = TOTAL_STEPS,
        title = "You're all set, $displayName",
        subtitle = "Here's your starting plan — change any of it later in Settings.",
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Start drinking",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        SummaryCard(state = state, liters = liters)
    }
}

@Composable
private fun SummaryCard(
    state: OnboardingFormState,
    liters: String,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        SummaryRowData(
            icon = DwIcons.Drop,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = "Daily goal",
            value = "$liters L",
        ),
        SummaryRowData(
            icon = DwIcons.Bell,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = "Reminder",
            value = "every ${state.intervalMinutes} min",
        ),
        SummaryRowData(
            icon = DwIcons.Sun,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = "Wake",
            value = state.wakeTime.label(),
        ),
        SummaryRowData(
            icon = DwIcons.Moon,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            label = "Bedtime",
            value = state.sleepTime.label(),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        rows.forEachIndexed { index, row ->
            SummaryRow(data = row)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

private data class SummaryRowData(
    val icon: ImageVector,
    val iconBg: androidx.compose.ui.graphics.Color,
    val iconTint: androidx.compose.ui.graphics.Color,
    val label: String,
    val value: String,
)

@Composable
private fun SummaryRow(
    data: SummaryRowData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(data.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(0.dp))
        Text(
            text = data.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = data.value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}


@DwPreview
@Composable
private fun StepDonePreview() {
    DrinkWaterAppTheme {
        StepDone(
            state = OnboardingFormState(
                name = "Alex",
                intervalMinutes = 45,
                computedGoalMl = 2200,
            ),
            onStart = {},
            onBack = {},
        )
    }
}
