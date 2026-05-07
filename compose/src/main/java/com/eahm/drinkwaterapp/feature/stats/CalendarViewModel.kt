package com.eahm.drinkwaterapp.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class CalendarViewModel(
    private val database: AppDatabase,
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    private val dao get() = database.getHistoryDao()

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
            combine(_nav, prefsStorage.getGoalAmount(), dao.getEarliestRecord()) { nav, goal, earliest ->
                Triple(nav, goal, earliest)
            }.flatMapLatest { (nav, goal, earliest) ->
                val firstRecordDate = earliest?.timestamp?.date

                if (nav.viewMode == CalendarViewMode.MONTH) {
                    val monthDate = nav.displayedMonth
                    val monthEnum = monthDate.month
                    val monthInt = monthEnum.ordinal + 1
                    val daysCount = daysInMonth(monthDate.year, monthInt)
                    val start = LocalDateTime(monthDate.year, monthInt, 1, 0, 0, 0, 0)
                    val end = LocalDateTime(monthDate.year, monthInt, daysCount, 23, 59, 59, 999999999)

                    dao.findByTimestamp(start, end).map { records ->
                        val grouped = records
                            .filter { it.amount > 0f }
                            .groupBy { it.timestamp.date }
                            .mapValues { (_, entries) ->
                                entries.sumOf { it.amount.toDouble() }.toFloat()
                            }

                        val days = (1..daysCount).map { day ->
                            val date = LocalDate(monthDate.year, monthEnum, day)
                            val amount = grouped[date] ?: 0f
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

                        CalendarUpdate(nav = nav, goal = goal, days = days, months = emptyList())
                    }
                } else {
                    val year = nav.displayedYear
                    val start = LocalDateTime(year, 1, 1, 0, 0, 0, 0)
                    val end = LocalDateTime(year, 12, 31, 23, 59, 59, 999999999)

                    dao.findByTimestamp(start, end).map { records ->
                        val grouped = records
                            .filter { it.amount > 0f }
                            .groupBy { it.timestamp.month }
                            .mapValues { (_, entries) ->
                                entries.sumOf { it.amount.toDouble() }.toFloat()
                            }

                        val months = Month.entries.map { month ->
                            val monthInt = month.ordinal + 1
                            val days = daysInMonth(year, monthInt)
                            val totalGoal = goal * days
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

                        CalendarUpdate(nav = nav, goal = goal, days = emptyList(), months = months)
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
