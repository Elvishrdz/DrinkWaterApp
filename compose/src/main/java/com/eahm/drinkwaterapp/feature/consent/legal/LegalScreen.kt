package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.LayoutTypesPreviewProvider

@Composable
internal fun LegalScreen(
    privacyToggleChecked: Boolean,
    showStepCounter: Boolean,
    layoutType: LayoutType = getLayoutType(),
    onToggleChanged: (Boolean) -> Unit,
    onAccept: () -> Unit,
) {
    when (layoutType) {
        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.PORTRAIT,
            -> LegalPortrait(
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.LANDSCAPE,
            -> LegalLandscape(
            weight = 0.7f,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED,
            -> LegalLandscape(
            weight = 1f,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )


        LayoutType.TABLE_TOP,
            -> LegalTableTop(
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
