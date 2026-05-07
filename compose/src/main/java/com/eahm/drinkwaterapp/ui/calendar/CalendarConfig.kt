package com.eahm.drinkwaterapp.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

data class DrinkCalendarConfig(
    val dayCircleSize: Dp = 34.dp,
    val monthCircleSize: Dp = 56.dp,
    val strokeWidth: Dp = 3.dp,
    val trackColor: Color = DwInk100,
    val progressColors: List<Color> = listOf(DwWaterLight, DwWaterDeep),
    val goalReachedColors: List<Color> = listOf(DwWaterLight, DwWaterDeep),
    val dayTextStyle: TextStyle = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = DwInk900,
    ),
    val headerTextStyle: TextStyle = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = DwInk900,
    ),
    val disabledAlpha: Float = 0.28f,
    val todayIndicatorColor: Color = DwMagenta,
)

object DrinkCalendarDefaults {
    @Composable
    fun config() = DrinkCalendarConfig()
}
