package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
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
        title = stringResource(R.string.onb_wake_title),
        subtitle = stringResource(R.string.onb_wake_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Sun) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            if (isSameTime) {
                Text(
                    text = stringResource(R.string.onb_wake_same_time_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
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

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepWakeLandscapePreview() {
    DrinkWaterAppTheme {
        StepWake(
            step = 3,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onWakeTimeChange = {},
            onNext = {},
            onBack = {},
        )
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepWakeTabletPreview() {
    DrinkWaterAppTheme {
        StepWake(
            step = 3,
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            onWakeTimeChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
