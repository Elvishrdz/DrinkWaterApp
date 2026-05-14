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
    val bgColor = if (isActive) DwSuccess.copy(alpha = 0.12f) else DwInk200
    val fgColor = if (isActive) DwSuccess else DwInk400
    val label =
        if (isActive) stringResource(R.string.drink_alarm_status_active) else stringResource(R.string.drink_alarm_status_resting)
    val icon = if (isActive) DwIcons.Sun else DwIcons.Moon

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