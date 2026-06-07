package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwSuccess
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun ActiveStatusBadge(isActive: Boolean) {
    val status = if (isActive) AlarmDayStatus.ACTIVE else AlarmDayStatus.RESTING
    ActiveStatusBadge(status)
}

@Composable
internal fun ActiveStatusBadge(status: AlarmDayStatus) {
    val bgColor = when (status) {
        AlarmDayStatus.ACTIVE -> DwSuccess.copy(alpha = 0.12f)
        AlarmDayStatus.RESTING -> DwInk200
        AlarmDayStatus.NO_ALARMS_TODAY -> DwInk200
    }
    val fgColor = when (status) {
        AlarmDayStatus.ACTIVE -> DwSuccess
        AlarmDayStatus.RESTING -> DwInk400
        AlarmDayStatus.NO_ALARMS_TODAY -> DwInk400
    }
    val label = when (status) {
        AlarmDayStatus.ACTIVE -> stringResource(R.string.drink_alarm_status_active)
        AlarmDayStatus.RESTING -> stringResource(R.string.drink_alarm_status_resting)
        AlarmDayStatus.NO_ALARMS_TODAY -> stringResource(R.string.drink_alarm_status_no_alarms_today)
    }
    val icon = when (status) {
        AlarmDayStatus.ACTIVE -> DwIcons.Sun
        AlarmDayStatus.RESTING -> DwIcons.Moon
        AlarmDayStatus.NO_ALARMS_TODAY -> DwIcons.Bell
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fgColor,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = fgColor,
        )
    }
}

@Preview
@Composable
private fun ActiveStatusBadgePreview() {
    DrinkWaterAppTheme {
        ActiveStatusBadge(true)
    }
}

internal enum class AlarmDayStatus { ACTIVE, RESTING, NO_ALARMS_TODAY }