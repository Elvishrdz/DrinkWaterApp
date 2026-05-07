package com.eahm.drinkwaterapp.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val MONTH_LABELS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

@Composable
fun YearCalendarGrid(
    months: List<CalendarMonthData>,
    config: DrinkCalendarConfig = DrinkCalendarDefaults.config(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(4) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(3) { col ->
                    val index = row * 3 + col
                    val month = months.getOrNull(index)
                    if (month != null) {
                        MonthCircle(
                            month = month,
                            config = config,
                            label = MONTH_LABELS.getOrElse(index) { "" },
                        )
                    }
                }
            }
        }
    }
}
