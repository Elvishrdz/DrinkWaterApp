package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell
import com.eahm.drinkwaterapp.feature.settings.ui.IntervalBottomSheet

private data class IntervalOption(val minutes: Int, val title: String, val subtitle: String)

private val presetMinutes = setOf(30, 45, 60, 120)

@Composable
internal fun StepInterval(
    step: Int,
    selectedMinutes: Int,
    wakeTime: OnbTimeSelection,
    sleepTime: OnbTimeSelection,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
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
    var showCustomSheet by rememberSaveable { mutableStateOf(false) }

    val windowMinutes = remember(wakeTime, sleepTime) {
        val startMin = wakeTime.toHour24() * 60 + wakeTime.minute
        val endMin = sleepTime.toHour24() * 60 + sleepTime.minute
        if (endMin > startMin) endMin - startMin else (24 * 60 - startMin) + endMin
    }

    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_interval_title),
        subtitle = stringResource(R.string.onb_interval_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Clock) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
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
            ChoiceCard(
                title = stringResource(R.string.onb_interval_custom_title),
                subtitle = if (isCustomSelected) formatCustomLabel(selectedMinutes) else stringResource(
                    R.string.onb_interval_custom_subtitle
                ),
                selected = isCustomSelected,
                onClick = { showCustomSheet = true },
                leadingIcon = DwIcons.Settings,
            )
        }
    }

    if (showCustomSheet) {
        IntervalBottomSheet(
            currentMinutes = if (isCustomSelected) selectedMinutes else 45,
            windowMinutes = windowMinutes,
            onIntervalChanged = onSelect,
            onDismiss = { showCustomSheet = false },
        )
    }
}

private fun formatCustomLabel(minutes: Int): String = when {
    minutes < 60 -> "Every $minutes min"
    minutes % 60 == 0 -> "Every ${minutes / 60}h"
    else -> "Every ${minutes / 60}h ${minutes % 60}min"
}


@DwPreview
@Composable
private fun StepIntervalPreview() {
    DrinkWaterAppTheme {
        StepInterval(
            step = 1,
            selectedMinutes = 45,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepIntervalCustomSelectedPreview() {
    DrinkWaterAppTheme {
        StepInterval(
            step = 1,
            selectedMinutes = 20,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepIntervalLandscapePreview() {
    DrinkWaterAppTheme {
        StepInterval(
            step = 5,
            selectedMinutes = 45,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepIntervalTabletPreview() {
    DrinkWaterAppTheme {
        StepInterval(
            step = 5,
            selectedMinutes = 45,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}
