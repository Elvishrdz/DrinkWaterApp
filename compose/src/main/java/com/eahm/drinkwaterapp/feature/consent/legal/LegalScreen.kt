package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.LayoutTypesPreviewProvider

internal val legalHeaderGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to DwWaterLight,
        0.5f to DwWater,
        1.0f to DwWaterDeep,
    )
)

@Composable
internal fun LegalScreen(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    layoutType: LayoutType = getLayoutType(),
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    when (layoutType) {
        LayoutType.PORTRAIT -> LegalPortrait(
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_EXPANDED -> LegalLandscape(
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.TABLE_TOP -> LegalTableTop(
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )
    }
}

@Preview
@Composable
private fun LegalScreenPreview(
    @PreviewParameter(LayoutTypesPreviewProvider::class) layoutType: LayoutType,
) {
    DrinkWaterAppTheme {
        LegalScreen(
            privacyToggleChecked = false,
            showStepCounter = true,
            layoutType = layoutType,
            onToggleChanged = {},
            onAccept = {},
        )
    }
}
