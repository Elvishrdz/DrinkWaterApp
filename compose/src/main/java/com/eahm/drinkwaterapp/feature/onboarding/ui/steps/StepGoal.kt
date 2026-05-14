package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

@Composable
internal fun StepGoalResult(
    step: Int,
    goalMl: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val liters = goalMl / 1000.0
    val glasses = (goalMl / 250.0).toInt()

    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_goal_result_title),
        subtitle = stringResource(R.string.onb_goal_result_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Drop) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = stringResource(R.string.onb_goal_result_cta),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        GoalDisplay(goalMl = goalMl, liters = liters, glasses = glasses)
    }
}

@Composable
private fun GoalDisplay(
    goalMl: Int,
    liters: Double,
    glasses: Int,
    modifier: Modifier = Modifier,
) {
    val numGradient = Brush.verticalGradient(listOf(DwWater, DwWaterDeep))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Big litres number
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 72.sp,
                        brush = numGradient,
                    )
                ) { append("%.2f".format(liters)) }
                withStyle(
                    SpanStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        brush = numGradient,
                    )
                ) { append(" L") }
            },
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        // mL equivalent
        Text(
            text = "${"%,d".format(goalMl)} mL",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        // Glasses equivalent
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.onb_goal_result_glasses_about))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$glasses glasses") }
                append(stringResource(R.string.onb_goal_result_glasses_suffix))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}


@DwPreview
@Composable
private fun StepGoalResultPreview() {
    DrinkWaterAppTheme {
        StepGoalResult(
            step = 1,
            goalMl = 2200,
            onNext = {},
            onBack = {},
        )
    }
}
