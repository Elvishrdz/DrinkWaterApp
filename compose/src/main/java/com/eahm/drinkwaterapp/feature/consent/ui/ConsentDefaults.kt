package com.eahm.drinkwaterapp.feature.consent.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight

internal object ConsentDefaults {

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

    val overlapHeightDp = 22.dp

    val topRoundedShape = RoundedCornerShape(
        topStart = overlapHeightDp,
        topEnd = overlapHeightDp
    )

    val shadowElevationDp = 8.dp
    val contentPaddingDp = 22.dp
    val verticalSpacingDp = 14.dp
}
