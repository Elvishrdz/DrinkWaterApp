package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.domain.extension.sanitizeZeroDecimals

@Composable
fun MiniStatsCards(
    drinkStreakDays: Int,
    bestDrinkStreakDays: Int?,
    goalStreakDays: Int,
    bestGoalStreakDays: Int?,
    personalBestMl: Float?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (FeatureFlags.streakFeatureEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MiniCard(
                    icon = DwIcons.Drop,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    value = drinkStreakDays.toString(),
                    label = stringResource(R.string.stats_mini_drink_streak),
                    modifier = Modifier.weight(1f),
                )
                bestDrinkStreakDays?.let {
                    MiniCard(
                        icon = DwIcons.Star,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        value = it.toString(),
                        label = stringResource(R.string.stats_mini_best_streak),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MiniCard(
                    icon = DwIcons.Star,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    value = goalStreakDays.toString(),
                    label = stringResource(R.string.stats_mini_goal_streak),
                    modifier = Modifier.weight(1f),
                )
                bestGoalStreakDays?.let {
                    MiniCard(
                        icon = DwIcons.Star,
                        iconBg = DwTintMagenta,
                        iconTint = DwMagenta,
                        value = it.toString(),
                        label = stringResource(R.string.stats_mini_best_goal_streak),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        personalBestMl?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MiniCard(
                    icon = DwIcons.Glass,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    value = it.sanitizeZeroDecimals(),
                    label = stringResource(R.string.stats_mini_personal_best),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun NoBestStatsPreview() {
    DrinkWaterAppTheme {
        MiniStatsCards(
            drinkStreakDays = 7,
            bestDrinkStreakDays = null,
            goalStreakDays = 4,
            bestGoalStreakDays = null,
            personalBestMl = 2950f,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun AllStatsPreview() {
    DrinkWaterAppTheme {
        MiniStatsCards(
            drinkStreakDays = 7,
            bestDrinkStreakDays = 12,
            goalStreakDays = 4,
            bestGoalStreakDays = 4,
            personalBestMl = 2950f,
            modifier = Modifier.padding(16.dp),
        )
    }
}
