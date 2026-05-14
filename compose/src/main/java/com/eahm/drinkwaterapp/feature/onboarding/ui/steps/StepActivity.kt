package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.ActivityLevel
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

private data class ActivityOption(
    val level: ActivityLevel,
    val title: String,
    val subtitle: String,
)

/** Step 6 — Activity level selection. */
@Composable
internal fun StepActivity(
    selected: ActivityLevel?,
    onSelect: (ActivityLevel) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val activityOptions = listOf(
        ActivityOption(
            ActivityLevel.LIGHT,
            stringResource(R.string.onb_activity_light_title),
            stringResource(R.string.onb_activity_light_subtitle)
        ),
        ActivityOption(
            ActivityLevel.MODERATE,
            stringResource(R.string.onb_activity_moderate_title),
            stringResource(R.string.onb_activity_moderate_subtitle)
        ),
        ActivityOption(
            ActivityLevel.ACTIVE,
            stringResource(R.string.onb_activity_active_title),
            stringResource(R.string.onb_activity_active_subtitle)
        ),
    )

    OnbShell(
        step = 6,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_activity_title),
        subtitle = stringResource(R.string.onb_activity_subtitle),
        art = { OnbArtCircle(icon = OnbIcons.ActivityRun) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = stringResource(R.string.onb_cta_continue),
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
