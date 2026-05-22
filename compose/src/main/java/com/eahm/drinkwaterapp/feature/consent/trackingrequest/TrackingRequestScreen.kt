package com.eahm.drinkwaterapp.feature.consent.trackingrequest

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.LayoutTypesPreviewProvider

internal val trackMagentaGradient = Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))

internal val trackOverlapHeightDp = 22.dp
internal val trackTopRoundedShape = RoundedCornerShape(
    topStart = trackOverlapHeightDp,
    topEnd = trackOverlapHeightDp,
)
internal val trackShadowElevationDp = 8.dp
internal val trackContentPaddingDp = 22.dp
internal val trackVerticalSpacingDp = 14.dp

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
