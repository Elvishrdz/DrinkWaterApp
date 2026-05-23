package com.eahm.drinkwaterapp.feature.onboarding.ui.shell.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.BodyCornerRadius

internal object OnbShellDefaults {

    val legalGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to DwWaterLight,
            0.5f to DwWater,
            1.0f to DwWaterDeep,
        )
    )

    val trackingGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFB83A95),
            1.0f to DwMagentaDark,
        )
    )

    val overlapHeightDp = 30.dp

    val topRoundedShape = RoundedCornerShape(
        topStart = BodyCornerRadius,
        topEnd = BodyCornerRadius,
    )

    val externalPaddingDp = 7.dp
    val shadowElevationDp = 8.dp
    val contentPaddingDp = 22.dp
    val verticalSpacingDp = 14.dp
}
