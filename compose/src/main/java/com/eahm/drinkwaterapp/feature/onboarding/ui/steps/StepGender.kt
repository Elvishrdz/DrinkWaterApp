package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.Gender
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.icons.OnbIcons
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

/** Step 4 — Gender selection (optional, improves goal calculation). */
@Composable
internal fun StepGender(
    selected: Gender?,
    onSelect: (Gender) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = 4,
        total = OnboardingSteps.totalSteps,
        title = "A bit about you",
        subtitle = "Helps us refine your hydration formula. You can skip this.",
        art = { OnbArtCircle(icon = OnbIcons.Person) },
        onBack = onBack,
        footer = {
            DwPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceCard(
                title = "Female",
                selected = selected == Gender.FEMALE,
                onClick = { onSelect(Gender.FEMALE) },
                leadingIcon = OnbIcons.GenderFemale,
            )
            ChoiceCard(
                title = "Male",
                selected = selected == Gender.MALE,
                onClick = { onSelect(Gender.MALE) },
                leadingIcon = OnbIcons.GenderMale,
            )
            ChoiceCard(
                title = "Prefer not to say",
                selected = selected == Gender.PREFER_NOT_TO_SAY,
                onClick = { onSelect(Gender.PREFER_NOT_TO_SAY) },
                leadingIcon = OnbIcons.GenderNeutral,
            )
        }
    }
}


@DwPreview
@Composable
private fun StepGenderPreview() {
    DrinkWaterAppTheme {
        StepGender(
            selected = Gender.FEMALE,
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}
