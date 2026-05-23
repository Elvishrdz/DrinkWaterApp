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
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.OnbTimeWheel
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle

@Composable
internal fun stepWake(
    wakeTime: OnbTimeSelection,
    sleepTime: OnbTimeSelection,
    is24Hour: Boolean,
    onWakeTimeChange: (OnbTimeSelection) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Sun) },
    title = stringResource(R.string.onb_wake_title),
    subtitle = stringResource(R.string.onb_wake_subtitle),
    onBack = onBack,
    footer = {
        val isSameTime = wakeTime.toActiveTime() == sleepTime.toActiveTime()

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
    content = {
        OnbTimeWheel(
            time = wakeTime,
            onTimeChange = onWakeTimeChange,
            is24Hour = is24Hour,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        )
    },
)

@Composable
private fun StepWakeBasePreview() {
    OnboardingStepBasePreview(
        config = stepWake(
            wakeTime = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
            sleepTime = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
            is24Hour = false,
            onWakeTimeChange = {},
            onNext = {},
            onBack = {},
        ),
    )
}

@DwPreview
@Composable
private fun StepWakePreview() {
    DrinkWaterAppTheme {
        StepWakeBasePreview()
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepWakeLandscapePreview() {
    DrinkWaterAppTheme {
        StepWakeBasePreview()
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepWakeTabletPreview() {
    DrinkWaterAppTheme {
        StepWakeBasePreview()
    }
}
