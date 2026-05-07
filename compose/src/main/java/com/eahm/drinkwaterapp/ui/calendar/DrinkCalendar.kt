package com.eahm.drinkwaterapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.stats.CalendarState

@Composable
fun DrinkCalendar(
    state: CalendarState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onViewModeToggle: (CalendarViewMode) -> Unit,
    config: DrinkCalendarConfig = DrinkCalendarDefaults.config(),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Header row: nav arrows + range label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = DwIcons.ChevronRight,
                    contentDescription = "Previous",
                    tint = DwInk900,
                    modifier = Modifier.graphicsLayer(rotationZ = 180f),
                )
            }

            Text(
                text = state.rangeLabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DwInk900,
            )

            IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = DwIcons.ChevronRight,
                    contentDescription = "Next",
                    tint = DwInk900,
                )
            }
        }

        // View mode toggle aligned to the end
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            CalendarViewToggle(
                viewMode = state.viewMode,
                onViewModeToggle = onViewModeToggle,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (state.viewMode) {
            CalendarViewMode.MONTH -> MonthCalendarGrid(
                days = state.days,
                today = state.today,
                config = config,
                modifier = Modifier.fillMaxWidth(),
            )
            CalendarViewMode.YEAR -> YearCalendarGrid(
                months = state.months,
                config = config,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CalendarViewToggle(
    viewMode: CalendarViewMode,
    onViewModeToggle: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(CalendarViewMode.MONTH to "Month", CalendarViewMode.YEAR to "Year")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DwInk50)
            .padding(3.dp),
    ) {
        Row {
            options.forEach { (mode, label) ->
                val isActive = mode == viewMode
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .then(
                            if (isActive) Modifier
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(999.dp),
                                    ambientColor = Color(0x14000000),
                                    spotColor = Color(0x14000000),
                                )
                                .background(DwPaper)
                            else Modifier
                        )
                        .clickable { onViewModeToggle(mode) }
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                ) {
                    Text(
                        text = label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isActive) DwInk900 else DwInk500,
                    )
                }
            }
        }
    }
}
