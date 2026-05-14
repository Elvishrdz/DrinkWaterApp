package com.eahm.drinkwaterapp.feature.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime

@Composable
internal fun HourGroupCard(
    group: HourGroup,
    expandedId: String?,
    onToggleExpand: (String) -> Unit,
    onEdit: (HistoryUI) -> Unit,
    onDelete: (HistoryUI) -> Unit,
) {
    val groupTotalMl = group.items.sumOf { it.amount.toIntOrNull() ?: 0 }
    val morning = stringResource(R.string.history_part_morning)
    val afternoon = stringResource(R.string.history_part_afternoon)
    val evening = stringResource(R.string.history_part_evening)
    val night = stringResource(R.string.history_part_night)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwPaper),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HourPill(hour = group.hour)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = partOfDay(group.hour, morning, afternoon, evening, night),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk500,
            )
            Text(
                text = " · ",
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk300,
            )
            Text(
                text = stringResource(R.string.drink_log_entry_amount, groupTotalMl.toString()),
                fontFamily = com.eahm.drinkwaterapp.core.designsystem.theme.Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DwInk500,
            )
        }

        HorizontalDivider(color = DwInk100)

        group.items.forEachIndexed { index, item ->
            RecordRow(
                item = item,
                isExpanded = expandedId == item.id,
                onToggle = { onToggleExpand(item.id) },
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item) },
            )
            if (index < group.items.lastIndex) {
                HorizontalDivider(color = DwInk100)
            }
        }
    }
}

@Composable
internal fun HourPill(hour: Int) {
    val (bgColor, fgColor) = when {
        hour in 5..7 -> DwTintMagenta to DwMagenta
        hour in 8..16 -> DwTintWater to DwWater
        hour in 17..19 -> DwTintMagenta to DwMagenta
        else -> DwInk100 to DwInk500
    }
    val icon = if (hour in 5..19) DwIcons.Sun else DwIcons.Moon
    val amLabel = stringResource(R.string.history_time_am)
    val pmLabel = stringResource(R.string.history_time_pm)

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
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = hourLabel(hour, amLabel, pmLabel),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = fgColor,
        )
    }
}

private fun partOfDay(
    hour: Int,
    morning: String,
    afternoon: String,
    evening: String,
    night: String
): String = when {
    hour in 5..11 -> morning
    hour in 12..16 -> afternoon
    hour in 17..20 -> evening
    else -> night
}

private fun hourLabel(hour: Int, am: String, pm: String): String {
    val period = if (hour < 12) am else pm
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$h12 $period"
}

private val previewGroup = HourGroup(
    hour = 8,
    isCurrentHour = false,
    items = listOf(
        HistoryUI("1", "250", 1, LocalDateTime(2026, 5, 10, 8, 30)),
        HistoryUI("2", "300", 1, LocalDateTime(2026, 5, 10, 8, 52)),
    ),
)

@Preview(showBackground = true)
@Composable
private fun HourGroupCardCollapsedPreview() {
    DrinkWaterAppTheme {
        HourGroupCard(
            group = previewGroup,
            expandedId = null,
            onToggleExpand = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HourGroupCardExpandedPreview() {
    DrinkWaterAppTheme {
        HourGroupCard(
            group = previewGroup,
            expandedId = "1",
            onToggleExpand = {},
            onEdit = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HourPillMorningPreview() {
    DrinkWaterAppTheme { HourPill(hour = 8) }
}

@Preview(showBackground = true)
@Composable
private fun HourPillAfternoonPreview() {
    DrinkWaterAppTheme { HourPill(hour = 14) }
}

@Preview(showBackground = true)
@Composable
private fun HourPillNightPreview() {
    DrinkWaterAppTheme { HourPill(hour = 23) }
}
