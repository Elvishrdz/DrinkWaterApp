package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    bestStreakDays: Int,
    drinkStreakDays: Int,
    personalBestMl: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiniCard(
                icon = DwIcons.Star,
                iconBg = DwTintMagenta,
                iconTint = DwMagenta,
                value = streakDays.toString(),
                label = "Goal streak",
                modifier = Modifier.weight(1f),
            )
            MiniCard(
                icon = DwIcons.Star,
                iconBg = DwTintWater,
                iconTint = DwWater,
                value = bestStreakDays.toString(),
                label = "Best streak",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiniCard(
                icon = DwIcons.Drop,
                iconBg = DwTintWater,
                iconTint = DwWater,
                value = drinkStreakDays.toString(),
                label = "Drink streak",
                modifier = Modifier.weight(1f),
            )
            MiniCard(
                icon = DwIcons.Glass,
                iconBg = DwTintWater,
                iconTint = DwWater,
                value = personalBestMl.toString(),
                label = "Personal best (mL)",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun MiniStatsCardsPreview() {
    DrinkWaterAppTheme {
        MiniStatsCards(
            streakDays = 4,
            bestStreakDays = 12,
            drinkStreakDays = 7,
            personalBestMl = 2950,
            modifier = Modifier.padding(16.dp),
        )
    }
}
