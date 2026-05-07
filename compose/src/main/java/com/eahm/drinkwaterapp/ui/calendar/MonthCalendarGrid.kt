package com.eahm.drinkwaterapp.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate

private val WEEKDAY_HEADERS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

@Composable
fun MonthCalendarGrid(
    days: List<CalendarDayData>,
    today: LocalDate,
    config: DrinkCalendarConfig = DrinkCalendarDefaults.config(),
    modifier: Modifier = Modifier,
) {
    val cellSize = config.dayCircleSize
    val offset = if (days.isEmpty()) 0 else {
        // DayOfWeek ordinal: MONDAY=0 … SUNDAY=6 → directly the column offset
        days.first().date.dayOfWeek.ordinal
    }

    Column(modifier = modifier) {
        // Weekday header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            WEEKDAY_HEADERS.forEach { header ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(cellSize),
                ) {
                    Text(text = header, style = config.headerTextStyle)
                }
            }
        }

        // Day cells
        val totalCells = offset + days.size
        val rowCount = (totalCells + 6) / 7

        repeat(rowCount) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(7) { col ->
                    val index = row * 7 + col - offset
                    val day = days.getOrNull(index)
                    if (day != null) {
                        DayCircle(
                            day = day,
                            today = today,
                            config = config,
                        )
                    } else {
                        Box(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}
