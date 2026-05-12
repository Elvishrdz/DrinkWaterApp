package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.TOTAL_STEPS
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

private data class IntervalOption(val minutes: Int, val title: String, val subtitle: String)

private val intervalOptions = listOf(
    IntervalOption(30, "Every 30 minutes", "Most consistent"),
    IntervalOption(45, "Every 45 minutes", "Recommended"),
    IntervalOption(60, "Every hour", "Light touch"),
    IntervalOption(120, "Every 2 hours", "Minimal"),
)

/** Step 9 — Reminder interval selection. */
@Composable
internal fun StepInterval(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = 9,
        total = TOTAL_STEPS,
        title = "How often should we remind you?",
        subtitle = "You can change this anytime in Settings.",
        art = { OnbArtCircle(icon = DwIcons.Clock) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            intervalOptions.forEach { option ->
                ChoiceCard(
                    title = option.title,
                    subtitle = option.subtitle,
                    selected = selectedMinutes == option.minutes,
                    onClick = { onSelect(option.minutes) },
                    leadingIcon = DwIcons.Clock,
                )
            }
        }
    }
}


@DwPreview
@Composable
private fun StepIntervalPreview() {
    DrinkWaterAppTheme {
        StepInterval(
            selectedMinutes = 45,
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}
