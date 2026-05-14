package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

/** Step 3 — Name input. */
@Composable
internal fun StepName(
    step: Int,
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_name_title),
        subtitle = stringResource(R.string.onb_name_subtitle),
        art = { OnbArtCircle(icon = OnbIcons.Person) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = stringResource(R.string.onb_cta_continue),
                onClick = onNext,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.onb_name_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@DwPreview
@Composable
private fun StepNamePreview() {
    DrinkWaterAppTheme {
        StepName(
            step = 1,
            name = "Alex",
            onNameChange = {},
            onNext = {},
            onBack = {},
        )
    }
}
