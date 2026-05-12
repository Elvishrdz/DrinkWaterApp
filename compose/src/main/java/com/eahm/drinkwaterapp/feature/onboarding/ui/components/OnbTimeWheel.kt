package com.eahm.drinkwaterapp.feature.onboarding.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection

/**
 * Scrollable time-picker wheel showing hour : minute.
 *
 * Displays in 24-hour format (0–23) and converts to/from [OnbTimeSelection] (12-hour + AM/PM).
 */
@Composable
internal fun OnbTimeWheel(
    time: OnbTimeSelection,
    onTimeChange: (OnbTimeSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PickHourMinute(
            initialHour = time.toHour24(),
            onHourChange = { newHour24 ->
                val isPm = newHour24 >= 12
                val hour12 = when {
                    newHour24 == 0 -> 12
                    newHour24 > 12 -> newHour24 - 12
                    else -> newHour24
                }
                onTimeChange(time.copy(hour = hour12, isPm = isPm))
            },
            initialMinute = time.minute,
            onMinuteChange = { newMinute ->
                onTimeChange(time.copy(minute = newMinute))
            },
            containerColor = Color.Transparent,
            focusIndicator = PickTimeFocusIndicator(
                enabled = true,
                widthFull = false,
                background = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, DwWater),
            ),
            selectedTextStyle = PickTimeTextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
            ),
            unselectedTextStyle = PickTimeTextStyle(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                fontSize = 18.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}


@DwPreview
@Composable
private fun OnbTimeWheelPreview() {
    DrinkWaterAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            OnbTimeWheel(
                time = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
                onTimeChange = {},
            )
        }
    }
}
