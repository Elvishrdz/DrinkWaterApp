package com.eahm.drinkwaterapp.feature.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk200
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk700
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwSuccess
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.history.v2.DayItem

@Composable
internal fun DayStripRow(
    days: List<DayItem>,
    selectedKey: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedKey) {
        val index = days.indexOfFirst { it.key == selectedKey }
        if (index >= 0) {
            listState.animateScrollToItem(index = maxOf(0, index - 2))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DwPaper)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPrevDay,
            enabled = canGoPrev,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Previous day",
                tint = if (canGoPrev) DwInk500 else DwInk200,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180f),
            )
        }

        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(days, key = { it.key }) { day ->
                DayChip(
                    day = day,
                    isSelected = day.key == selectedKey,
                    onClick = { if (day.isEnabled) onSelectDay(day.key) },
                )
            }
        }

        IconButton(
            onClick = onNextDay,
            enabled = canGoNext,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Next day",
                tint = if (canGoNext) DwInk500 else DwInk200,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
internal fun DayChip(
    day: DayItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dimmed = !day.isEnabled
    val bgColor = if (isSelected) DwMagenta else Color.Transparent
    val primaryTextColor = when {
        isSelected -> DwPaper
        dimmed -> DwInk200
        else -> DwInk700
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = day.isEnabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = day.weekday,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = primaryTextColor.copy(alpha = if (isSelected) 0.75f else 1f),
        )
        Text(
            text = day.dayNum,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = primaryTextColor,
        )
        if (day.goalMet) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) DwPaper else DwSuccess),
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}



private val previewDays = listOf(
    DayItem("2026-05-07", "Wed", "7", "Wed, May 7", false, false, false),
    DayItem("2026-05-08", "Thu", "8", "Thu, May 8", false, true, true),
    DayItem("2026-05-09", "Fri", "9", "Fri, May 9", false, true, false),
    DayItem("2026-05-10", "Sat", "10", "Today, May 10", true, true, false),
    DayItem("2026-05-11", "Sun", "11", "Sun, May 11", false, false, false),
)

@Preview(showBackground = true)
@Composable
private fun DayStripRowPreview() {
    DrinkWaterAppTheme {
        DayStripRow(
            days = previewDays,
            selectedKey = "2026-05-10",
            canGoPrev = true,
            canGoNext = false,
            onSelectDay = {},
            onPrevDay = {},
            onNextDay = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayChipSelectedPreview() {
    DrinkWaterAppTheme {
        DayChip(
            day = DayItem("2026-05-10", "Sat", "10", "Today, May 10", true, true, false),
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayChipGoalMetPreview() {
    DrinkWaterAppTheme {
        DayChip(
            day = DayItem("2026-05-08", "Thu", "8", "Thu, May 8", false, true, true),
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayChipDisabledPreview() {
    DrinkWaterAppTheme {
        DayChip(
            day = DayItem("2026-05-11", "Sun", "11", "Sun, May 11", false, false, false),
            isSelected = false,
            onClick = {},
        )
    }
}
