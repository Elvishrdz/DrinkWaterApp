package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.TOTAL_STEPS
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.OnbTimeWheel
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

/** Step 8 — Bedtime picker. */
@Composable
internal fun StepSleep(
    sleepTime: OnbTimeSelection,
    onSleepTimeChange: (OnbTimeSelection) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = 8,
        total = TOTAL_STEPS,
        title = "And bedtime?",
        subtitle = "Reminders pause an hour before so they don't keep you awake.",
        art = { OnbArtCircle(icon = DwIcons.Moon) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        OnbTimeWheel(
            time = sleepTime,
            onTimeChange = onSleepTimeChange,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}


@DwPreview
@Composable
private fun StepSleepPreview() {
    DrinkWaterAppTheme {
        StepSleep(
            sleepTime = OnbTimeSelection(hour = 10, minute = 30, isPm = true),
            onSleepTimeChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
