package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MiniStatsCards(
    drinkStreakDays: Int,
    bestDrinkStreakDays: Int?,
    goalStreakDays: Int,
    bestGoalStreakDays: Int?,
    personalBestMl: Float?,
    modifier: Modifier = Modifier,
    personalBestDate: LocalDate? = null,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

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
            val daysAgo = personalBestDate?.let { date ->
                (today.toEpochDays() - date.toEpochDays()).toInt()
            }
            var tooltipVisible by rememberSaveable { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniCard(
                        icon = DwIcons.Glass,
                        iconBg = DwTintWater,
                        iconTint = DwWater,
                        value = it.formatVolume(unitPreference),
                        label = stringResource(R.string.stats_mini_personal_best),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = if (personalBestDate != null) {
                            { tooltipVisible = !tooltipVisible }
                        } else null,
                    )
                    if (tooltipVisible && personalBestDate != null && daysAgo != null) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { tooltipVisible = false },
                        ) {
                            PersonalBestDateTooltip(
                                date = personalBestDate,
                                daysAgo = daysAgo,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalBestDateTooltip(
    date: LocalDate,
    daysAgo: Int,
) {
    val mainText = when {
        daysAgo == 0 -> stringResource(R.string.stats_best_date_today)
        daysAgo > 37 -> stringResource(
            R.string.stats_best_date_achieved,
            formatPersonalBestDateLong(date)
        )

        else -> formatPersonalBestDateShort(date)
    }

    val relativeText = when {
        daysAgo == 0 || daysAgo > 37 -> null
        daysAgo < 30 -> pluralStringResource(R.plurals.stats_best_date_days_ago, daysAgo, daysAgo)
        else -> {
            val months = daysAgo / 30
            pluralStringResource(R.plurals.stats_best_date_months_ago, months, months)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(DwInk900)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = mainText,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
        )
        if (relativeText != null) {
            Text(
                text = relativeText,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = DwInk300,
            )
        }
    }
}

private fun formatPersonalBestDateShort(date: LocalDate): String {
    val calendar = Calendar.getInstance().apply {
        set(date.year, date.monthNumber - 1, date.dayOfMonth)
    }
    return SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun formatPersonalBestDateLong(date: LocalDate): String {
    val calendar = Calendar.getInstance().apply {
        set(date.year, date.monthNumber - 1, date.dayOfMonth)
    }
    return DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(calendar.time)
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
