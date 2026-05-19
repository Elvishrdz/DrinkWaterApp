package com.eahm.drinkwaterapp.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.streak.StreakData
import com.eahm.drinkwaterapp.core.data.streak.StreakRepository
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class StatsViewModel(
    database: AppDatabase,
    streakRepository: StreakRepository,
    prefsStorage: PrefsStorage,
) : ViewModel() {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)
    private val _chartPage = MutableStateFlow(0)

    private val pages: StateFlow<List<PeriodPage>> = _selectedPeriod
        .flatMapLatest { period -> loadPagesFlow(period) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val streakData: StateFlow<StreakData> = streakRepository.streakData
        .stateIn(viewModelScope, SharingStarted.Lazily, StreakData())

    val state: StateFlow<StatsState> = combine(
        combine(
            _selectedPeriod,
            _chartPage,
            pages,
        ) { period, page, pages ->
            val safeIndex = page.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
            val summary = computeSummary(pages, safeIndex)
            StatsState(
                selectedPeriod = period,
                chartPage = page,
                pages = pages,
                avgMl = summary.avgMl,
                avgTrendPercent = summary.trendPercent,
                isTrendPositive = summary.isTrendPositive,
                showTrend = summary.showTrend,
            )
        },
        streakData,
    ) { partialState, streak ->
        partialState.copy(
            drinkStreakDays = streak.drinkStreakDays,
            bestDrinkStreakDays = streak.bestDrinkStreakDays,
            goalStreakDays = streak.goalStreakDays,
            bestGoalStreakDays = streak.bestGoalStreakDays,
            personalBestMl = streak.personalBestMl,
        )
    }.combine(prefsStorage.getUnitPreference()) { state, unitPref ->
        state.copy(unitPreference = unitPref)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = StatsState(),
    )

    fun onPeriodSelected(period: StatsPeriod) {
        _selectedPeriod.value = period
        _chartPage.value = Int.MAX_VALUE // clamps to pages.size - 1 (newest page)
    }

    fun onChartPageChanged(page: Int) {
        _chartPage.value = page
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    private data class Summary(
        val avgMl: Int,
        val trendPercent: Int,
        val isTrendPositive: Boolean,
        val showTrend: Boolean,
    )

    /**
     * Average intake and period-over-period trend for [pageIndex].
     * pages[0] = oldest, pages[N] = most recent.
     * Trend compares against the immediately preceding page (pageIndex − 1).
     *
     * ── avgMl ────────────────────────────────────────────────────────────────
     * Average mL on days/months where at least something was recorded
     * (zeros excluded). Represents the user's typical intake on a drinking day.
     *
     * ── trendPercent ─────────────────────────────────────────────────────────
     * Percentage change in *total* consumption vs the previous period:
     *
     *   trendPercent = (currentTotal − prevTotal) / prevTotal × 100
     *
     * Why totals instead of per-active-day averages?
     * Comparing averages-of-active-days divides by a different number of active
     * days each period (e.g. 1 logging day last week vs 5 this week), which
     * inflates the percentage dramatically even when total volume barely changed.
     * Totals give an honest "did I drink more or less overall?" answer,
     * independent of how many individual log entries existed.
     *
     * Example: 500 mL/day × 7 days this week = 3500 mL total.
     *          500 mL/day × 7 days last week  = 3500 mL total.
     *          → trendPercent = 0  →  UI shows the "consistent" message.
     *
     * Special cases:
     *   • prevTotal == 0  → trendPercent = 0 (guarded upstream by showTrend,
     *     so the trend row is hidden in the UI when no prior data exists).
     *   • trendPercent == 0 → the UI replaces "0 %" with a positive message
     *     ("Excellent! You matched last week's records. Keep it up!").
     *
     * ── showTrend ────────────────────────────────────────────────────────────
     * True only when at least one prior page contains recorded data. Prevents
     * showing any comparison on a fresh install where there is nothing to compare.
     */
    private fun computeSummary(pages: List<PeriodPage>, pageIndex: Int): Summary {
        if (pages.isEmpty() || pageIndex !in pages.indices) {
            return Summary(avgMl = 0, trendPercent = 0, isTrendPositive = true, showTrend = false)
        }

        // avgMl: average across all days in the period, but only counting elapsed days
        // for the current (in-progress) period so future days don't drag the average down.
        val isCurrentPeriod = pages[pageIndex].entries.any { it.isToday }
        val periodEntries = if (isCurrentPeriod) {
            // Only count days from the start of the period up to and including today.
            val todayIdx = pages[pageIndex].entries.indexOfFirst { it.isToday }
            pages[pageIndex].entries.take(todayIdx + 1)
        } else {
            pages[pageIndex].entries
        }
        val avg = if (periodEntries.isEmpty()) 0
        else periodEntries.sumOf { it.amountMl } / periodEntries.size

        // showTrend: only show when at least one prior page has recorded data
        val showTrend = pageIndex > 0 && (0 until pageIndex).any { i ->
            pages[i].entries.any { it.amountMl > 0 }
        }

        // trendPercent: total consumption this period vs total consumption last period
        val currentTotal = pages[pageIndex].entries.sumOf { it.amountMl }
        val prevTotal = pages.getOrNull(pageIndex - 1)?.entries?.sumOf { it.amountMl } ?: 0

        val trendPercent =
            if (prevTotal > 0) ((currentTotal - prevTotal).toDouble() / prevTotal * 100).toInt() else 0

        return Summary(
            avgMl = avg,
            trendPercent = abs(trendPercent),
            isTrendPositive = trendPercent >= 0,
            showTrend = showTrend,
        )
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private fun loadPagesFlow(period: StatsPeriod): Flow<List<PeriodPage>> = when (period) {
        StatsPeriod.WEEK -> loadWeekPages()
        StatsPeriod.MONTH -> loadMonthPages()
        StatsPeriod.YEAR -> loadYearPages()
    }

    /** 8 pages, oldest first. Each page = Mon–Sun of a calendar week. */
    private fun loadWeekPages(): Flow<List<PeriodPage>> {
        val daysFromMonday = today.dayOfWeek.ordinal  // MONDAY=0 … SUNDAY=6
        val thisMonday = today.minus(daysFromMonday, DateTimeUnit.DAY)
        val rangeStart = thisMonday.minus((NUM_WEEKS - 1) * 7, DateTimeUnit.DAY)

        return combine(
            historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay()),
            goalDao.getAllGoals(),
        ) { records, allGoals ->
            val dailyTotals = records.toDailyTotals()
            // The earliest day the user ever set a goal — used to detect pre-app pages.
            val firstGoalDate = allGoals.minByOrNull { it.effectiveDate }?.effectiveDate
            (0 until NUM_WEEKS).map { weekIndex ->
                val monday = thisMonday.minus(weekIndex * 7, DateTimeUnit.DAY)
                val sunday = monday.plus(6, DateTimeUnit.DAY)
                // A page is "before first record" when the entire week ends before the
                // user's first daily-goal entry (i.e. the user hadn't installed the app yet).
                val isBeforeFirstRecord = firstGoalDate != null && sunday < firstGoalDate
                PeriodPage(
                    rangeLabel = buildWeekRangeLabel(monday, sunday),
                    entries = (0..6).map { dayOffset ->
                        val date = monday.plus(dayOffset, DateTimeUnit.DAY)
                        DayEntry(
                            shortLabel = DAY_SHORT_LABELS[dayOffset],
                            amountMl = (dailyTotals[date] ?: 0f).toInt(),
                            isToday = date == today,
                            goalMl = allGoals.effectiveGoalForDate(date),
                        )
                    },
                    isBeforeFirstRecord = isBeforeFirstRecord,
                )
            }.reversed() // pages[0] = oldest, pages[N] = most recent
        }
    }

    /** 6 pages, oldest first. Each page = all days of a calendar month. */
    private fun loadMonthPages(): Flow<List<PeriodPage>> {
        val monthStarts = (0 until NUM_MONTHS).map { offset ->
            var year = today.year
            var month = today.monthNumber - offset
            while (month <= 0) {
                month += 12; year--
            }
            LocalDate(year, month, 1)
        }
        val rangeStart = monthStarts.last()

        return combine(
            historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay()),
            goalDao.getAllGoals(),
        ) { records, allGoals ->
            val dailyTotals = records.toDailyTotals()
            monthStarts.map { monthStart ->
                val year = monthStart.year
                val month = monthStart.month
                val daysCount = daysInMonth(year, monthStart.monthNumber)
                PeriodPage(
                    rangeLabel = buildMonthLabel(month, year),
                    entries = (1..daysCount).map { day ->
                        val date = LocalDate(year, month, day)
                        DayEntry(
                            shortLabel = day.toString(),
                            amountMl = (dailyTotals[date] ?: 0f).toInt(),
                            isToday = date == today,
                            goalMl = allGoals.effectiveGoalForDate(date),
                        )
                    },
                )
            }.reversed() // pages[0] = oldest, pages[N] = most recent
        }
    }

    /** 3 pages, oldest first. Each page = 12 months of a calendar year. */
    private fun loadYearPages(): Flow<List<PeriodPage>> {
        val years = (0 until NUM_YEARS).map { today.year - it }
        val rangeStart = LocalDate(years.last(), 1, 1)

        return combine(
            historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay()),
            goalDao.getAllGoals(),
        ) { records, allGoals ->
            val monthlyTotals = records
                .filter { it.amount > 0f }
                .groupBy { Pair(it.timestamp.year, it.timestamp.monthNumber) }
                .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }

            years.map { year ->
                PeriodPage(
                    rangeLabel = year.toString(),
                    entries = (1..12).map { monthNum ->
                        val daysCount = daysInMonth(year, monthNum)
                        val lastDay = LocalDate(year, monthNum, daysCount)
                        DayEntry(
                            shortLabel = MONTH_SHORT_LABELS[monthNum - 1],
                            amountMl = (monthlyTotals[Pair(year, monthNum)] ?: 0f).toInt(),
                            isToday = year == today.year && monthNum == today.monthNumber,
                            goalMl = allGoals.effectiveGoalForDate(lastDay) * daysCount,
                        )
                    },
                )
            }.reversed() // pages[0] = oldest, pages[N] = most recent
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun List<HistoryEntity>.toDailyTotals(): Map<LocalDate, Float> =
        filter { it.amount > 0f }
            .groupBy { it.timestamp.date }
            .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }

    /**
     * Returns the effective daily goal for [date]: the most recent goal whose
     * effective_date is on or before [date], or [DEFAULT_GOAL_ML] if none exists.
     */
    private fun List<DailyGoalEntity>.effectiveGoalForDate(date: LocalDate): Int =
        filter { it.effectiveDate <= date }
            .maxByOrNull { it.effectiveDate }
            ?.goalMl?.toInt()
            ?: DEFAULT_GOAL_ML.toInt()

    private fun LocalDate.atStartOfDay() = LocalDateTime(year, monthNumber, dayOfMonth, 0, 0, 0, 0)
    private fun LocalDate.atEndOfDay() =
        LocalDateTime(year, monthNumber, dayOfMonth, 23, 59, 59, 999_999_999)

    private fun buildWeekRangeLabel(monday: LocalDate, sunday: LocalDate): String {
        val start = "${MONTH_SHORT_LABELS[monday.monthNumber - 1]} ${monday.dayOfMonth}"
        val end = if (monday.month == sunday.month) {
            "${sunday.dayOfMonth}"
        } else {
            "${MONTH_SHORT_LABELS[sunday.monthNumber - 1]} ${sunday.dayOfMonth}"
        }
        return "$start – $end"
    }

    private fun buildMonthLabel(month: Month, year: Int): String =
        "${MONTH_FULL_LABELS[month.ordinal]} $year"

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    companion object {
        private const val NUM_WEEKS = 8
        private const val NUM_MONTHS = 6
        private const val NUM_YEARS = 3

        private val DAY_SHORT_LABELS: List<String> =
            DateFormatSymbols(Locale.getDefault()).shortWeekdays.let { dfs ->
                listOf(dfs[2], dfs[3], dfs[4], dfs[5], dfs[6], dfs[7], dfs[1]) // Mon–Sun
            }
        private val MONTH_SHORT_LABELS: List<String> =
            DateFormatSymbols(Locale.getDefault()).shortMonths.take(12)
        private val MONTH_FULL_LABELS: List<String> =
            DateFormatSymbols(Locale.getDefault()).months.take(12)
    }
}
