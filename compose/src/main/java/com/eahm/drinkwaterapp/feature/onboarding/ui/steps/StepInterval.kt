package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

private data class IntervalOption(val minutes: Int, val title: String, val subtitle: String)

private val presetMinutes = setOf(30, 45, 60, 120)

@Composable
internal fun stepInterval(
    selectedMinutes: Int,
    wakeTime: OnbTimeSelection,
    sleepTime: OnbTimeSelection,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onCustomInterval: (Int, Int) -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Clock) },
    title = stringResource(R.string.onb_interval_title),
    subtitle = stringResource(R.string.onb_interval_subtitle),
    onBack = onBack,
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.onb_cta_continue),
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        val intervalOptions = listOf(
            IntervalOption(
                30,
                stringResource(R.string.onb_interval_30_title),
                stringResource(R.string.onb_interval_30_subtitle)
            ),
            IntervalOption(
                45,
                stringResource(R.string.onb_interval_45_title),
                stringResource(R.string.onb_interval_45_subtitle)
            ),
            IntervalOption(
                60,
                stringResource(R.string.onb_interval_60_title),
                stringResource(R.string.onb_interval_60_subtitle)
            ),
            IntervalOption(
                120,
                stringResource(R.string.onb_interval_120_title),
                stringResource(R.string.onb_interval_120_subtitle)
            ),
        )

        val isCustomSelected = selectedMinutes !in presetMinutes
        // todo: var showCustomSheet by rememberSaveable { mutableStateOf(false) }

        val windowMinutes = remember(wakeTime, sleepTime) {
            val startMin = wakeTime.toHour24() * 60 + wakeTime.minute
            val endMin = sleepTime.toHour24() * 60 + sleepTime.minute
            if (endMin > startMin) endMin - startMin else (24 * 60 - startMin) + endMin
        }

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
            ChoiceCard(
                title = stringResource(R.string.onb_interval_custom_title),
                subtitle = if (isCustomSelected) formatCustomLabel(selectedMinutes)
                else stringResource(R.string.onb_interval_custom_subtitle),
                selected = isCustomSelected,
                onClick = {
                    onCustomInterval(
                        if (isCustomSelected) selectedMinutes else 45,
                        windowMinutes
                    )
                },
                leadingIcon = DwIcons.Settings,
            )
        }
    },
)

private fun formatCustomLabel(minutes: Int): String = when {
    minutes < 60 -> "Every $minutes min"
    minutes % 60 == 0 -> "Every ${minutes / 60}h"
    else -> "Every ${minutes / 60}h ${minutes % 60}min"
}

@Composable
private fun StepIntervalBasePreview(
    selectedMinutes: Int,
) {
    OnboardingStepBasePreview(
        config = stepInterval(
            selectedMinutes = selectedMinutes,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onSelect = {},
            onNext = {},
            onBack = {},
            onCustomInterval = { _, _ -> },
        ),
    )
}

@DwPreview
@Composable
private fun StepIntervalPreview() {
    DrinkWaterAppTheme {
        StepIntervalBasePreview(selectedMinutes = 45)
    }
}

@DwPreview
@Composable
private fun StepIntervalCustomPreview() {
    DrinkWaterAppTheme {
        StepIntervalBasePreview(selectedMinutes = 20)
    }
}
