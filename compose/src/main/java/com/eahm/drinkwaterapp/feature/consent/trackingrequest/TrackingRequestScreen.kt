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
        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.PORTRAIT,
            -> TrackingRequestPortrait(
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.LANDSCAPE,
            -> TrackingRequestLandscape(
            weight = 0.7f,
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )

        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED,
            -> TrackingRequestLandscape(
            weight = 1f,
            isTrackingEnabled = isTrackingEnabled,
            showStepCounter = showStepCounter,
            onAllow = onAllow,
            onDecline = onDecline,
        )


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
