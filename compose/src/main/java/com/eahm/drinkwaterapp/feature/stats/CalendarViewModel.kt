package com.eahm.drinkwaterapp.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.ui.calendar.CalendarDayData
import com.eahm.drinkwaterapp.ui.calendar.CalendarMonthData
import com.eahm.drinkwaterapp.ui.calendar.CalendarViewMode
import com.eahm.drinkwaterapp.ui.calendar.DayDisplayState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class CalendarViewModel(
    private val database: AppDatabase,
) : ViewModel() {

    private val historyDao get() = database.getHistoryDao()
    private val goalDao get() = database.getDailyGoalDao()

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    private data class NavKey(
        val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
        val displayedMonth: LocalDate,
        val displayedYear: Int,
    )

    private val _nav = MutableStateFlow(
        NavKey(
            displayedMonth = LocalDate(today.year, today.month, 1),
            displayedYear = today.year,
        )
    )

    private val _state = MutableStateFlow(
        CalendarState(
            displayedMonth = LocalDate(today.year, today.month, 1),
            displayedYear = today.year,
            today = today,
            rangeLabel = buildMonthLabel(today.month, today.year),
        )
    )
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    init {
        observeCalendarData()
    }

    fun navigatePrevious() {
        _nav.update { nav ->
            if (nav.viewMode == CalendarViewMode.MONTH) {
                nav.copy(displayedMonth = nav.displayedMonth.previousMonth())
            } else {
                nav.copy(displayedYear = nav.displayedYear - 1)
            }
        }
    }

    fun navigateNext() {
        _nav.update { nav ->
            if (nav.viewMode == CalendarViewMode.MONTH) {
                nav.copy(displayedMonth = nav.displayedMonth.nextMonth())
            } else {
                nav.copy(displayedYear = nav.displayedYear + 1)
            }
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        _nav.update { it.copy(viewMode = mode) }
    }

    private fun observeCalendarData() {
        viewModelScope.launch {
            combine(_nav, historyDao.getEarliestRecord()) { nav, earliest ->
                Pair(nav, earliest)
            }.flatMapLatest { (nav, earliest) ->
                val firstRecordDate = earliest?.timestamp?.date

                if (nav.viewMode == CalendarViewMode.MONTH) {
                    val monthDate = nav.displayedMonth
                    val monthEnum = monthDate.month
                    val monthInt = monthEnum.ordinal + 1
                    val daysCount = daysInMonth(monthDate.year, monthInt)
                    val start = LocalDateTime(monthDate.year, monthInt, 1, 0, 0, 0, 0)
                    val end =
                        LocalDateTime(monthDate.year, monthInt, daysCount, 23, 59, 59, 999999999)

                    val startDate = LocalDate(monthDate.year, monthInt, 1)
                    val endDate = LocalDate(monthDate.year, monthInt, daysCount)
                    val startEpoch = startDate.toEpochDays()
                    val endEpoch = endDate.toEpochDays()

                    historyDao.findByTimestamp(start, end).map { records ->
                        val grouped = records
                            .filter { it.amount > 0f }
                            .groupBy { it.timestamp.date }
                            .mapValues { (_, entries) ->
                                entries.sumOf { it.amount.toDouble() }.toFloat()
                            }

                        val goalRecords = goalDao.getGoalsInRange(startEpoch, endEpoch).first() +
                                listOfNotNull(goalDao.getGoalForDate(startEpoch - 1).first())
                        val goalMap = buildGoalMap(goalRecords, startDate, endDate)

                        val days = (1..daysCount).map { day ->
                            val date = LocalDate(monthDate.year, monthEnum, day)
                            val amount = grouped[date] ?: 0f
                            val goal = goalMap[date] ?: DEFAULT_GOAL_ML
                            CalendarDayData(
                                date = date,
                                amountMl = amount,
                                goalMl = goal,
                                displayState = computeDayDisplayState(
                                    date = date,
                                    amount = amount,
                                    goalMl = goal,
                                    today = today,
                                    firstRecordDate = firstRecordDate,
                                ),
                            )
                        }

                        val displayGoal = goalMap[today] ?: DEFAULT_GOAL_ML
                        CalendarUpdate(
                            nav = nav,
                            goal = displayGoal,
                            days = days,
                            months = emptyList()
                        )
                    }
                } else {
                    val year = nav.displayedYear
                    val start = LocalDateTime(year, 1, 1, 0, 0, 0, 0)
                    val end = LocalDateTime(year, 12, 31, 23, 59, 59, 999999999)

                    val startDate = LocalDate(year, 1, 1)
                    val endDate = LocalDate(year, 12, 31)
                    val startEpoch = startDate.toEpochDays()
                    val endEpoch = endDate.toEpochDays()

                    historyDao.findByTimestamp(start, end).map { records ->
                        val grouped = records
                            .filter { it.amount > 0f }
                            .groupBy { it.timestamp.month }
                            .mapValues { (_, entries) ->
                                entries.sumOf { it.amount.toDouble() }.toFloat()
                            }

                        val goalRecords = goalDao.getGoalsInRange(startEpoch, endEpoch).first() +
                                listOfNotNull(goalDao.getGoalForDate(startEpoch - 1).first())
                        val goalMap = buildGoalMap(goalRecords, startDate, endDate)

                        val months = Month.entries.map { month ->
                            val monthInt = month.ordinal + 1
                            val days = daysInMonth(year, monthInt)
                            val totalGoal = (1..days).sumOf { day ->
                                val date = LocalDate(year, month, day)
                                (goalMap[date] ?: DEFAULT_GOAL_ML).toDouble()
                            }.toFloat()
                            val totalAmount = grouped[month] ?: 0f
                            val firstDay = LocalDate(year, month, 1)
                            val lastDay = LocalDate(year, month, days)
                            CalendarMonthData(
                                month = month,
                                year = year,
                                totalAmountMl = totalAmount,
                                totalGoalMl = totalGoal,
                                displayState = computeMonthDisplayState(
                                    firstDay = firstDay,
                                    lastDay = lastDay,
                                    totalAmount = totalAmount,
                                    totalGoal = totalGoal,
                                    today = today,
                                    firstRecordDate = firstRecordDate,
                                ),
                            )
                        }

                        val displayGoal = goalMap[today] ?: DEFAULT_GOAL_ML
                        CalendarUpdate(
                            nav = nav,
                            goal = displayGoal,
                            days = emptyList(),
                            months = months
                        )
                    }
                }
            }.collectLatest { update ->
                val label = if (update.nav.viewMode == CalendarViewMode.MONTH) {
                    buildMonthLabel(update.nav.displayedMonth.month, update.nav.displayedMonth.year)
                } else {
                    update.nav.displayedYear.toString()
                }
                _state.update {
                    it.copy(
                        viewMode = update.nav.viewMode,
                        displayedMonth = update.nav.displayedMonth,
                        displayedYear = update.nav.displayedYear,
                        days = update.days,
                        months = update.months,
                        goalMl = update.goal,
                        rangeLabel = label,
                    )
                }
            }
        }
    }

    /**
     * Builds a map of LocalDate → effective goal by walking the sorted goal records and
     * carrying each goal forward until the next change boundary.
     */
    private fun buildGoalMap(
        goalRecords: List<DailyGoalEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Map<LocalDate, Float> {
        val sorted = goalRecords.sortedBy { it.effectiveDate }
        val result = mutableMapOf<LocalDate, Float>()

        var currentGoal = DEFAULT_GOAL_ML
        var recordIndex = 0

        var date = startDate
        while (date <= endDate) {
            // Advance through records whose effective_date <= current date
            while (recordIndex < sorted.size && sorted[recordIndex].effectiveDate <= date) {
                currentGoal = sorted[recordIndex].goalMl
                recordIndex++
            }
            result[date] = currentGoal
            date = date.plus(1, DateTimeUnit.DAY)
        }

        return result
    }

    private data class CalendarUpdate(
        val nav: NavKey,
        val goal: Float,
        val days: List<CalendarDayData>,
        val months: List<CalendarMonthData>,
    )

    private fun computeDayDisplayState(
        date: LocalDate,
        amount: Float,
        goalMl: Float,
        today: LocalDate,
        firstRecordDate: LocalDate?,
    ): DayDisplayState = when {
        firstRecordDate != null && date < firstRecordDate -> DayDisplayState.BEFORE_FIRST_RECORD
        date > today -> DayDisplayState.FUTURE
        amount == 0f -> DayDisplayState.NO_DATA
        amount >= goalMl -> DayDisplayState.GOAL_REACHED
        else -> DayDisplayState.PARTIAL
    }

    private fun computeMonthDisplayState(
        firstDay: LocalDate,
        lastDay: LocalDate,
        totalAmount: Float,
        totalGoal: Float,
        today: LocalDate,
        firstRecordDate: LocalDate?,
    ): DayDisplayState = when {
        firstRecordDate != null && lastDay < firstRecordDate -> DayDisplayState.BEFORE_FIRST_RECORD
        firstDay > today -> DayDisplayState.FUTURE
        totalAmount == 0f -> DayDisplayState.NO_DATA
        totalAmount >= totalGoal -> DayDisplayState.GOAL_REACHED
        else -> DayDisplayState.PARTIAL
    }

    private fun LocalDate.previousMonth(): LocalDate {
        val isJanuary = month == Month.JANUARY
        val newMonth = if (isJanuary) Month.DECEMBER else Month.entries[month.ordinal - 1]
        val newYear = if (isJanuary) year - 1 else year
        return LocalDate(newYear, newMonth, 1)
    }

    private fun LocalDate.nextMonth(): LocalDate {
        val isDecember = month == Month.DECEMBER
        val newMonth = if (isDecember) Month.JANUARY else Month.entries[month.ordinal + 1]
        val newYear = if (isDecember) year + 1 else year
        return LocalDate(newYear, newMonth, 1)
    }

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    private fun buildMonthLabel(month: Month, year: Int): String {
        val name = when (month) {
            Month.JANUARY -> "January"; Month.FEBRUARY -> "February"
            Month.MARCH -> "March"; Month.APRIL -> "April"
            Month.MAY -> "May"; Month.JUNE -> "June"
            Month.JULY -> "July"; Month.AUGUST -> "August"
            Month.SEPTEMBER -> "September"; Month.OCTOBER -> "October"
            Month.NOVEMBER -> "November"; else -> "December"
        }
        return "$name $year"
    }
}
