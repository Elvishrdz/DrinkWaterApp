package com.eahm.drinkwaterapp.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaperWarm
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.stats.components.MiniStatsCards
import com.eahm.drinkwaterapp.feature.stats.components.SummaryCard
import com.eahm.drinkwaterapp.feature.stats.components.WeeklyBarChart
import com.eahm.drinkwaterapp.ui.calendar.CalendarViewMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = koinViewModel(),
    calendarViewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val calendarState by calendarViewModel.state.collectAsState()

    Scaffold { innerPadding ->
        StatsContent(
            state = state,
            calendarState = calendarState,
            onPeriodSelected = { period ->
                viewModel.onPeriodSelected(period)
                // Keep calendar view mode in sync: year toggle → year calendar, else → month
                calendarViewModel.setViewMode(
                    if (period == StatsPeriod.YEAR) CalendarViewMode.YEAR else CalendarViewMode.MONTH
                )
            },
            onChartPageChanged = viewModel::onChartPageChanged,
            onCalendarPrevious = calendarViewModel::navigatePrevious,
            onCalendarNext = calendarViewModel::navigateNext,
            onCalendarViewModeToggle = calendarViewModel::setViewMode,
            modifier = Modifier
                .padding(innerPadding),
        )
    }
}

@Composable
internal fun StatsContent(
    state: StatsState,
    calendarState: CalendarState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onChartPageChanged: (Int) -> Unit,
    onCalendarPrevious: () -> Unit,
    onCalendarNext: () -> Unit,
    onCalendarViewModeToggle: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periodLabel = when (state.selectedPeriod) {
        StatsPeriod.WEEK -> "This week's"
        StatsPeriod.MONTH -> "This month's"
        StatsPeriod.YEAR -> "This year's"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DwPaperWarm),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stats",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = DwInk900
                )
                SegmentedControl(
                    options = StatsPeriod.entries.map { it.label },
                    selectedIndex = state.selectedPeriod.ordinal,
                    onSelected = { index -> onPeriodSelected(StatsPeriod.entries[index]) }
                )
            }
        }

        item {
            SummaryCard(
                avgMl = state.avgMl,
                trendPercent = state.avgTrendPercent,
                isTrendPositive = state.isTrendPositive,
                periodLabel = periodLabel,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        item {
            WeeklyBarChart(
                period = state.selectedPeriod,
                pages = state.pages,
                goalMl = state.goalMl,
                onPageChanged = onChartPageChanged,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        item {
            MiniStatsCards(
                streakDays = state.drinkStreakDays,
                bestStreakDays = state.bestStreakDays,
                goalStreakDays = state.streakDays,
                bestGoalStreakDays = null, // TODO: track best goal streak
                personalBestMl = state.personalBestMl,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

//        item {
//            DrinkCalendar(
//                state = calendarState,
//                onPrevious = onCalendarPrevious,
//                onNext = onCalendarNext,
//                onViewModeToggle = onCalendarViewModeToggle,
//                modifier = Modifier.padding(horizontal = 18.dp),
//            )
//        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DwInk50)
            .padding(3.dp)
    ) {
        Row {
            options.forEachIndexed { index, label ->
                val isActive = index == selectedIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .then(
                            if (isActive) Modifier
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(999.dp),
                                    ambientColor = androidx.compose.ui.graphics.Color(0x14000000),
                                    spotColor = androidx.compose.ui.graphics.Color(0x14000000),
                                )
                                .background(DwPaper)
                            else Modifier
                        )
                        .clickable { onSelected(index) }
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = label,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isActive) DwInk900 else DwInk500
                    )
                }
            }
        }
    }
}

private val previewState = StatsState(
    selectedPeriod = StatsPeriod.WEEK,
    pages = listOf(
        PeriodPage(
            rangeLabel = "Apr 27 – May 3",
            entries = listOf(
                DayEntry("Mon", 2200),
                DayEntry("Tue", 1500, isToday = true),
                DayEntry("Wed", 0),
                DayEntry("Thu", 0),
                DayEntry("Fri", 0),
                DayEntry("Sat", 0),
                DayEntry("Sun", 0),
            )
        ),
        PeriodPage(
            rangeLabel = "Apr 20 – Apr 26",
            entries = listOf(
                DayEntry("Mon", 2300),
                DayEntry("Tue", 2200),
                DayEntry("Wed", 2550),
                DayEntry("Thu", 2100),
                DayEntry("Fri", 2400),
                DayEntry("Sat", 1900),
                DayEntry("Sun", 2600),
            )
        ),
    ),
    goalMl = 2400,
    avgMl = 1850,
    avgTrendPercent = 12,
    isTrendPositive = true,
    streakDays = 4,
    bestStreakDays = 12,
    drinkStreakDays = 7,
    bestDrinkStreakDays = 20,
    personalBestMl = 2950,
)

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    DrinkWaterAppTheme {
        StatsContent(
            state = previewState,
            calendarState = CalendarState(
                displayedMonth = kotlinx.datetime.LocalDate(2026, 5, 1),
                displayedYear = 2026,
                today = kotlinx.datetime.LocalDate(2026, 5, 7),
                rangeLabel = "May 2026",
            ),
            onPeriodSelected = {},
            onChartPageChanged = {},
            onCalendarPrevious = {},
            onCalendarNext = {},
            onCalendarViewModeToggle = {},
        )
    }
}
