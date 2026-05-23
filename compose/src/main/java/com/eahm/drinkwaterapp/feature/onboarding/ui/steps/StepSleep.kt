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
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.OnbTimeWheel
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

@Composable
internal fun stepSleep(
    wakeTime: OnbTimeSelection,
    sleepTime: OnbTimeSelection,
    is24Hour: Boolean,
    onSleepTimeChange: (OnbTimeSelection) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Moon) },
    title = stringResource(R.string.onb_sleep_title),
    subtitle = stringResource(R.string.onb_sleep_subtitle),
    onBack = onBack,
    footer = {
        val isSameTime = sleepTime.toActiveTime() == wakeTime.toActiveTime()
        if (isSameTime) {
            Text(
                text = stringResource(R.string.onb_sleep_same_time_error),
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
    content = {
        OnbTimeWheel(
            time = sleepTime,
            onTimeChange = onSleepTimeChange,
            is24Hour = is24Hour,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
    },
)

@Composable
private fun StepSleepBasePreview() {
    OnboardingStepBasePreview(
        config = stepSleep(
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 30, isPm = true),
            is24Hour = false,
            onSleepTimeChange = {},
            onNext = {},
            onBack = {},
        ),
    )
}

@DwPreview
@Composable
private fun StepSleepPreview() {
    DrinkWaterAppTheme {
        StepSleepBasePreview()
    }
}
