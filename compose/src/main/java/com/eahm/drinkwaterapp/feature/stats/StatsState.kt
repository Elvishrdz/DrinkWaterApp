package com.eahm.drinkwaterapp.feature.stats

import com.eahm.drinkwaterapp.core.presentation.UnitPreference

data class StatsState(
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val chartPage: Int = 0,
    val pages: List<PeriodPage> = emptyList(),
    val avgMl: Int = 0,
    val avgTrendPercent: Int = 0,
    val isTrendPositive: Boolean = true,
    val showTrend: Boolean = false,
    val drinkStreakDays: Int = 0,
    val bestDrinkStreakDays: Int? = null,
    val goalStreakDays: Int = 0,
    val bestGoalStreakDays: Int? = null,
    val personalBestMl: Float? = null,
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
)
