package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater

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
