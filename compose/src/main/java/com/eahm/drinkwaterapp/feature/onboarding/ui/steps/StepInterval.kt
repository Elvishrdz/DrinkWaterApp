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
import androidx.compose.ui.unit.dp
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

private val intervalOptions = listOf(
    IntervalOption(30, "Every 30 minutes", "Most consistent"),
    IntervalOption(45, "Every 45 minutes", "Recommended"),
    IntervalOption(60, "Every hour", "Light touch"),
    IntervalOption(120, "Every 2 hours", "Minimal"),
)

private val presetMinutes = intervalOptions.map { it.minutes }.toSet()

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
        title = "How often should we remind you?",
        subtitle = "You can change this anytime in Settings.",
        art = { OnbArtCircle(icon = DwIcons.Clock) },
        onBack = onBack,
        footer = {
            DwPrimaryButtonAnimated(
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
            ChoiceCard(
                title = "I'll choose",
                subtitle = if (isCustomSelected) formatCustomLabel(selectedMinutes) else "Pick your own interval",
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
