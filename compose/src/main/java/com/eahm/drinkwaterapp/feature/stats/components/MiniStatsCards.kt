package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
fun MiniStatsCards(
    streakDays: Int,
    personalBestMl: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniCard(
            icon = DwIcons.Star,
            iconBg = DwTintMagenta,
            iconTint = DwMagenta,
            value = streakDays.toString(),
            label = "Day streak",
            modifier = Modifier.weight(1f)
        )
        MiniCard(
            icon = DwIcons.Glass,
            iconBg = DwTintWater,
            iconTint = DwWater,
            value = personalBestMl.toString(),
            label = "Personal best (mL)",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .outlineBorderStyle()
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = value,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            lineHeight = (28 * 1.05).sp,
            color = DwInk900
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = DwInk500
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun MiniStatsCardsPreview() {
    DrinkWaterAppTheme {
        MiniStatsCards(
            streakDays = 4,
            personalBestMl = 2950,
            modifier = Modifier.padding(16.dp)
        )
    }
}
