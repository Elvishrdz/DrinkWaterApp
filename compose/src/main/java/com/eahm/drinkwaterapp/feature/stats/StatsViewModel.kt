package com.eahm.drinkwaterapp.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.streak.StreakData
import com.eahm.drinkwaterapp.core.data.streak.StreakRepository
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
) : ViewModel() {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)
    private val _chartPage = MutableStateFlow(0)

    private val todayGoalMl: StateFlow<Int> = goalDao
        .getGoalForDate(today.toEpochDays())
        .map { it?.goalMl?.toInt() ?: DEFAULT_GOAL_ML.toInt() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_GOAL_ML.toInt())

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
            todayGoalMl
        ) { period, page, pages, dailyGoalMl ->
            val chartGoal = when (period) {
                StatsPeriod.YEAR -> dailyGoalMl * 30
                else -> dailyGoalMl
            }
            val safeIndex = page.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
            val summary = computeSummary(pages, safeIndex)
            StatsState(
                selectedPeriod = period,
                chartPage = page,
                pages = pages,
                goalMl = chartGoal,
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
     * Only days/months with amountMl > 0 are counted.
     * pages[0] = oldest, pages[N] = most recent.
     * Trend compares against the previous (older) page at pageIndex - 1.
     *
     * [showTrend] is true only when at least one prior page has recorded data,
     * meaning the user has been using the app long enough to have a meaningful
     * comparison period. This prevents showing the trend on a fresh install.
     */
    private fun computeSummary(pages: List<PeriodPage>, pageIndex: Int): Summary {
        if (pages.isEmpty() || pageIndex !in pages.indices) {
            return Summary(avgMl = 0, trendPercent = 0, isTrendPositive = true, showTrend = false)
        }
        val amounts = pages[pageIndex].entries.map { it.amountMl }.filter { it > 0 }
        val avg = if (amounts.isNotEmpty()) amounts.average().toInt() else 0

        // Show trend only when there is at least one prior page with recorded data.
        // If every page before the current one is empty the app is just starting out.
        val showTrend = pageIndex > 0 && (0 until pageIndex).any { i ->
            pages[i].entries.any { it.amountMl > 0 }
        }

        val prevAmounts = pages.getOrNull(pageIndex - 1)
            ?.entries?.map { it.amountMl }?.filter { it > 0 }
        val prevAvg = if (!prevAmounts.isNullOrEmpty()) prevAmounts.average().toInt() else avg

        val trendPercent =
            if (prevAvg > 0) ((avg - prevAvg).toDouble() / prevAvg * 100).toInt() else 0

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

        return historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay())
            .map { records ->
                val dailyTotals = records.toDailyTotals()
                (0 until NUM_WEEKS).map { weekIndex ->
                    val monday = thisMonday.minus(weekIndex * 7, DateTimeUnit.DAY)
                    val sunday = monday.plus(6, DateTimeUnit.DAY)
                    PeriodPage(
                        rangeLabel = buildWeekRangeLabel(monday, sunday),
                        entries = (0..6).map { dayOffset ->
                            val date = monday.plus(dayOffset, DateTimeUnit.DAY)
                            DayEntry(
                                shortLabel = DAY_SHORT_LABELS[dayOffset],
                                amountMl = (dailyTotals[date] ?: 0f).toInt(),
                                isToday = date == today,
                            )
                        },
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

        return historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay())
            .map { records ->
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

        return historyDao.findByTimestamp(rangeStart.atStartOfDay(), today.atEndOfDay())
            .map { records ->
                val monthlyTotals = records
                    .filter { it.amount > 0f }
                    .groupBy { Pair(it.timestamp.year, it.timestamp.monthNumber) }
                    .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }

                years.map { year ->
                    PeriodPage(
                        rangeLabel = year.toString(),
                        entries = (1..12).map { monthNum ->
                            DayEntry(
                                shortLabel = MONTH_SHORT_LABELS[monthNum - 1],
                                amountMl = (monthlyTotals[Pair(year, monthNum)] ?: 0f).toInt(),
                                isToday = year == today.year && monthNum == today.monthNumber,
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
