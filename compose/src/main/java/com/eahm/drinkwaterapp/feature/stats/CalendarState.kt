package com.eahm.drinkwaterapp.feature.stats

import com.eahm.drinkwaterapp.ui.calendar.CalendarDayData
import com.eahm.drinkwaterapp.ui.calendar.CalendarMonthData
import com.eahm.drinkwaterapp.ui.calendar.CalendarViewMode
import kotlinx.datetime.LocalDate

data class CalendarState(
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val displayedMonth: LocalDate,
    val displayedYear: Int,
    val days: List<CalendarDayData> = emptyList(),
    val months: List<CalendarMonthData> = emptyList(),
    val today: LocalDate,
    val goalMl: Float = 0f,
    val rangeLabel: String = "",
)
