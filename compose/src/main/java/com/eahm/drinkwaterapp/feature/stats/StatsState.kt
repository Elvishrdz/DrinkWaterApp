package com.eahm.drinkwaterapp.feature.stats

import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.datetime.LocalDate

data class StatsState(
    val isAdFree: Boolean = false,
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val chartPage: Int = 0,
    val pages: List<PeriodPage> = emptyList(),
    val avgMl: Float = 0f,
    val avgTrendPercent: Int = 0,
    val isTrendPositive: Boolean = true,
    val showTrend: Boolean = false,
    val drinkStreakDays: Int = 0,
    val bestDrinkStreakDays: Int? = null,
    val goalStreakDays: Int = 0,
    val bestGoalStreakDays: Int? = null,
    val personalBestMl: Float? = null,
    val personalBestDate: LocalDate? = null,
    val unitPreference: UnitPreference = UnitPreference.ML,
)

enum class StatsPeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
}

data class DayEntry(
    val shortLabel: String,
    val amountMl: Int,
    val isToday: Boolean = false,
    val goalMl: Int = 0,
)

data class PeriodPage(
    val rangeLabel: String,
    val entries: List<DayEntry>,
    /**
     * Number of calendar days this page spans (7 for a week, 28–31 for a month,
     * 365/366 for a year). Used to compute the correct daily average in the summary.
     */
    val periodDays: Int = 0,
    /**
     * True when this page is entirely before the user's first daily-goal record.
     * The chart shows a "no data" placeholder instead of bars for these pages.
     */
    val isBeforeFirstRecord: Boolean = false,
)
