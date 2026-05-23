package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.settings.ui.GOAL_MAX
import com.eahm.drinkwaterapp.feature.settings.ui.GOAL_MIN
import com.eahm.drinkwaterapp.feature.settings.ui.GOAL_STEP
import com.eahm.drinkwaterapp.feature.settings.ui.GoalStepperRow

@Composable
internal fun stepSetGoal(
    goalMl: Float,
    onGoalChanged: (Float) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    unitPreference: UnitPreference = UnitPreference.ML,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Drop) },
    title = stringResource(R.string.onb_goal_title),
    subtitle = stringResource(R.string.onb_set_goal_subtitle),
    onBack = onBack,
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.onb_set_goal_cta),
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        var currentGoal by remember { mutableFloatStateOf(goalMl.coerceIn(GOAL_MIN, GOAL_MAX)) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GoalStepperRow(
                value = currentGoal,
                onDecrement = {
                    val next = (currentGoal - GOAL_STEP).coerceAtLeast(GOAL_MIN)
                    currentGoal = next
                    onGoalChanged(next)
                },
                onIncrement = {
                    val next = (currentGoal + GOAL_STEP).coerceAtMost(GOAL_MAX)
                    currentGoal = next
                    onGoalChanged(next)
                },
                onValueSet = { next ->
                    currentGoal = next
                    onGoalChanged(next)
                },
                unitPreference = unitPreference,
            )
        }
    },
)

@Composable
private fun StepSetGoalBasePreview() {
    OnboardingStepBasePreview(
        config = stepSetGoal(
            goalMl = 2200f,
            onGoalChanged = {},
            onNext = {},
            onBack = {},
        ),
    )
}

@DwPreview
@Composable
private fun StepSetGoalPreview() {
    DrinkWaterAppTheme {
        StepSetGoalBasePreview()
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun StepSetGoalLandscapePreview() {
    DrinkWaterAppTheme {
        StepSetGoalBasePreview()
    }
}

@Preview(name = "Tablet", widthDp = 1200, heightDp = 800)
@Composable
private fun StepSetGoalTabletPreview() {
    DrinkWaterAppTheme {
        StepSetGoalBasePreview()
    }
}
