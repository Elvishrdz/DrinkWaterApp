package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps
import com.eahm.drinkwaterapp.feature.onboarding.totalSteps
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.ChoiceCard
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell

@Composable
internal fun StepUnits(
    step: Int,
    selectedUnit: UnitPreference,
    onSelect: (UnitPreference) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    OnbShell(
        step = step,
        total = OnboardingSteps.totalSteps,
        title = stringResource(R.string.onb_units_title),
        subtitle = stringResource(R.string.onb_units_subtitle),
        art = { OnbArtCircle(icon = DwIcons.Drop) },
        onBack = onBack,
        useAdaptiveLayout = true,
        footer = {
            DwPrimaryButtonAnimated(
                text = stringResource(R.string.onb_cta_continue),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column {
            ChoiceCard(
                title = stringResource(R.string.onb_units_ml_title),
                subtitle = stringResource(R.string.onb_units_ml_subtitle),
                selected = selectedUnit == UnitPreference.ML || selectedUnit == UnitPreference.AUTO_LITERS,
                onClick = { onSelect(UnitPreference.ML) },
                leadingIcon = DwIcons.Drop,
            )
            ChoiceCard(
                title = stringResource(R.string.onb_units_fl_oz_title),
                subtitle = stringResource(R.string.onb_units_fl_oz_subtitle),
                selected = selectedUnit == UnitPreference.FL_OZ,
                onClick = { onSelect(UnitPreference.FL_OZ) },
                leadingIcon = DwIcons.Ruler,
            )
        }
    }
}

@DwPreview
@Composable
private fun StepUnitsMlPreview() {
    DrinkWaterAppTheme {
        StepUnits(
            step = 3,
            selectedUnit = UnitPreference.ML,
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}

@DwPreview
@Composable
private fun StepUnitsFlOzPreview() {
    DrinkWaterAppTheme {
        StepUnits(
            step = 3,
            selectedUnit = UnitPreference.FL_OZ,
            onSelect = {},
            onNext = {},
            onBack = {},
        )
    }
}
