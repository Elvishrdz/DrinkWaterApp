package com.eahm.drinkwaterapp.feature.consent.legal

import androidx.compose.foundation.layout.PaddingValues
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
    val innerPadding = PaddingValues()
    when (layoutType) {
        LayoutType.PORTRAIT -> LegalPortrait(
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_EXPANDED -> LegalLandscape(
            innerPadding = innerPadding,
            privacyToggleChecked = privacyToggleChecked,
            showStepCounter = showStepCounter,
            onToggleChanged = onToggleChanged,
            onAccept = onAccept,
        )

        LayoutType.TABLE_TOP -> LegalTableTop(
            innerPadding = innerPadding,
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
