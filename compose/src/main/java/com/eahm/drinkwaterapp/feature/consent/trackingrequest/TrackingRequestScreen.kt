package com.eahm.drinkwaterapp.feature.consent.trackingrequest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.LayoutTypesPreviewProvider

@Composable
internal fun TrackingRequestScreen(
    isTrackingEnabled: Boolean,
    showStepCounter: Boolean,
    layoutType: LayoutType = getLayoutType(),
    onAllow: () -> Unit,
    onDecline: () -> Unit,
) {
    when (layoutType) {
        LayoutType.PORTRAIT,
            -> TrackingRequestPortrait(
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.LANDSCAPE,
            -> TrackingRequestLandscape(
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.FOLDABLE_EXPANDED,
        LayoutType.TABLE_TOP,
            -> TrackingRequestTableTop(
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )
    }
}

@Preview
@Composable
private fun TrackingRequestScreenPreview(
    @PreviewParameter(LayoutTypesPreviewProvider::class) layoutType: LayoutType,
) {
    DrinkWaterAppTheme {
        TrackingRequestScreen(
            isTrackingEnabled = false,
            showStepCounter = true,
            layoutType = layoutType,
            onAllow = {},
            onDecline = {},
        )
    }
}
