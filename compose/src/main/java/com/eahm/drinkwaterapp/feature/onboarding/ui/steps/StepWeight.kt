package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.WeightUnit
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

/** Step 5 — Body weight input with lb / kg unit toggle. */
@Composable
internal fun StepWeight(
    weightValue: String,
    weightUnit: WeightUnit,
    onWeightChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = 5,
        total = OnboardingSteps.totalSteps,
        title = "What's your weight?",
        subtitle = "We use this to calculate your daily water goal.",
        art = { OnbArtCircle(icon = OnbIcons.Scale) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        WeightInputRow(
            value = weightValue,
            unit = weightUnit,
            onValueChange = onWeightChange,
            onUnitChange = onUnitChange,
        )
    }
}

@Composable
private fun WeightInputRow(
    value: String,
    unit: WeightUnit,
    onValueChange: (String) -> Unit,
    onUnitChange: (WeightUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numGradient = Brush.verticalGradient(listOf(DwWater, DwWaterDeep))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Large numeric field
        Box(
            modifier = Modifier
                .width(160.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { input ->
                    // Allow only digits and a single dot
                    val filtered = input.filter { it.isDigit() || it == '.' }
                        .let { s ->
                            val dotIndex = s.indexOf('.')
                            if (dotIndex >= 0) s.take(dotIndex + 1) + s.drop(dotIndex + 1)
                                .filter { it.isDigit() }
                            else s
                        }
                    onValueChange(filtered)
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    textAlign = TextAlign.Center,
                    brush = numGradient,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.displaySmall.copy(
                                textAlign = TextAlign.Center,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    inner()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // lb / kg pill toggle
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WeightUnit.entries.forEach { u ->
                val active = unit == u
                Text(
                    text = u.name.lowercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (active) DwWater else Color.Transparent)
                        .clickable { onUnitChange(u) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
    }
}


@DwPreview
@Composable
private fun StepWeightPreview() {
    DrinkWaterAppTheme {
        StepWeight(
            weightValue = "150",
            weightUnit = WeightUnit.LB,
            onWeightChange = {},
            onUnitChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
