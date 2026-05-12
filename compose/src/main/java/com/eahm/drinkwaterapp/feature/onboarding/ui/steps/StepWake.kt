package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.OnbTimeWheel
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

@Composable
internal fun StepWake(
    step: Int,
    wakeTime: OnbTimeSelection,
    sleepTime: OnbTimeSelection,
    onWakeTimeChange: (OnbTimeSelection) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val isSameTime = wakeTime.toActiveTime() == sleepTime.toActiveTime()

    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = "When do you wake up?",
        subtitle = "We'll start reminding you about 30 minutes after.",
        art = { OnbArtCircle(icon = DwIcons.Sun) },
        onBack = onBack,
        footer = {
            if (isSameTime) {
                Text(
                    text = "Wake-up and bedtime can't be the same.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
            DwPrimaryButtonAnimated(
                text = "Continue",
                onClick = onNext,
                enabled = !isSameTime,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        OnbTimeWheel(
            time = wakeTime,
            onTimeChange = onWakeTimeChange,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}


@DwPreview
@Composable
private fun StepWakePreview() {
    DrinkWaterAppTheme {
        StepWake(
            step = 1,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onWakeTimeChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
