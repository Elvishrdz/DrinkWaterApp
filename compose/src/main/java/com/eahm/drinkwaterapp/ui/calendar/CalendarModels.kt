package com.eahm.drinkwaterapp.ui.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

enum class CalendarViewMode { MONTH, YEAR }

enum class DayDisplayState {
    BEFORE_FIRST_RECORD,
    FUTURE,
    NO_DATA,
    PARTIAL,
    GOAL_REACHED,
}

data class CalendarDayData(
    val date: LocalDate,
    val amountMl: Float,
    val goalMl: Float,
    val displayState: DayDisplayState,
) {
    val progress: Float
        get() = if (goalMl > 0f) (amountMl / goalMl).coerceIn(0f, 1f) else 0f
}

data class CalendarMonthData(
    val month: Month,
    val year: Int,
    val totalAmountMl: Float,
    val totalGoalMl: Float,
    val displayState: DayDisplayState,
) {
    val progress: Float
        get() = if (totalGoalMl > 0f) (totalAmountMl / totalGoalMl).coerceIn(0f, 1f) else 0f
}
