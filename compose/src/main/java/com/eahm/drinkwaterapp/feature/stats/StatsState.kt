package com.eahm.drinkwaterapp.feature.stats

data class StatsState(
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val chartPage: Int = 0,
    val pages: List<PeriodPage> = emptyList(),
    val goalMl: Int = 2400,
    val avgMl: Int = 0,
    val avgTrendPercent: Int = 0,
    val isTrendPositive: Boolean = true,
    val streakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val drinkStreakDays: Int = 0,
    val bestDrinkStreakDays: Int = 0,
    val personalBestMl: Int = 0,
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
)

data class PeriodPage(
    val rangeLabel: String,
    val entries: List<DayEntry>,
)
