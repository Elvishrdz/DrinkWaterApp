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
import com.eahm.drinkwaterapp.feature.onboarding.ActivityLevel
import com.eahm.drinkwaterapp.feature.onboarding.TOTAL_STEPS
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

private data class ActivityOption(
    val level: ActivityLevel,
    val title: String,
    val subtitle: String,
)

private val activityOptions = listOf(
    ActivityOption(ActivityLevel.LIGHT, "Light", "Mostly sitting, little exercise"),
    ActivityOption(ActivityLevel.MODERATE, "Moderate", "Walks, light workouts 2–3×/week"),
    ActivityOption(ActivityLevel.ACTIVE, "Active", "Daily training or active job"),
)

/** Step 6 — Activity level selection. */
@Composable
internal fun StepActivity(
    selected: ActivityLevel?,
    onSelect: (ActivityLevel) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = 6,
        total = TOTAL_STEPS,
        title = "How active are you?",
        subtitle = "Active days need ~15% more water than rest days.",
        art = { OnbArtCircle(icon = OnbIcons.ActivityRun) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Continue",
                onClick = onNext,
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activityOptions.forEach { option ->
                ChoiceCard(
                    title = option.title,
                    subtitle = option.subtitle,
                    selected = selected == option.level,
                    onClick = { onSelect(option.level) },
                    leadingIcon = when (option.level) {
                        ActivityLevel.LIGHT -> DwIcons.Moon
                        ActivityLevel.MODERATE -> DwIcons.Sun
                        ActivityLevel.ACTIVE -> OnbIcons.ActivityRun
                    },
                )
            }
        }
    }
}


@DwPreview
@Composable
private fun StepActivityPreview() {
    DrinkWaterAppTheme {
        StepActivity(
            selected = ActivityLevel.MODERATE,
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}
