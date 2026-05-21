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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.ads.banner.InlineBannerAd
import com.eahm.drinkwaterapp.ads.banner.rememberStatsScreenComponentBannerAdState
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.stats.components.MiniStatsCards
import com.eahm.drinkwaterapp.feature.stats.components.SummaryCard
import com.eahm.drinkwaterapp.feature.stats.components.WeeklyBarChart
import com.eahm.drinkwaterapp.ui.calendar.CalendarViewMode
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun StatsScreen(
    viewModel: StatsViewModel = koinViewModel(),
    calendarViewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val calendarState by calendarViewModel.state.collectAsState()
    val purchaseManager = koinInject<PurchaseManager>()
    val isAdFree by purchaseManager.isAdFree.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        StatsContent(
            state = state,
            calendarState = calendarState,
            isAdFree = isAdFree,
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
    isAdFree: Boolean = true,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onChartPageChanged: (Int) -> Unit,
    onCalendarPrevious: () -> Unit,
    onCalendarNext: () -> Unit,
    onCalendarViewModeToggle: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bannerAdState = rememberStatsScreenComponentBannerAdState(enabled = !isAdFree)

    val periodLabelWeek = stringResource(R.string.stats_period_label_week)
    val periodLabelMonth = stringResource(R.string.stats_period_label_month)
    val periodLabelYear = stringResource(R.string.stats_period_label_year)
    val periodLabel = when (state.selectedPeriod) {
        StatsPeriod.WEEK -> periodLabelWeek
        StatsPeriod.MONTH -> periodLabelMonth
        StatsPeriod.YEAR -> periodLabelYear
    }

    val firstWeekLabel = stringResource(R.string.stats_summary_first_week)
    val firstMonthLabel = stringResource(R.string.stats_summary_first_month)
    val firstYearLabel = stringResource(R.string.stats_summary_first_year)
    val consistentWeekLabel = stringResource(R.string.stats_summary_consistent_week)
    val consistentMonthLabel = stringResource(R.string.stats_summary_consistent_month)
    val consistentYearLabel = stringResource(R.string.stats_summary_consistent_year)
    // overrideLabel replaces the trend percentage row:
    //  • first period  → introductory label (no prior data to compare)
    //  • 0 % change    → positive consistency message per period type
    //  • null          → show the numeric percentage normally
    val overrideLabel: String? = when {
        !state.showTrend -> when (state.selectedPeriod) {
            StatsPeriod.WEEK -> firstWeekLabel
            StatsPeriod.MONTH -> firstMonthLabel
            StatsPeriod.YEAR -> firstYearLabel
        }

        state.avgTrendPercent == 0 -> when (state.selectedPeriod) {
            StatsPeriod.WEEK -> consistentWeekLabel
            StatsPeriod.MONTH -> consistentMonthLabel
            StatsPeriod.YEAR -> consistentYearLabel
        }

        else -> null
    }

    val periodWeek = stringResource(R.string.stats_period_week)
    val periodMonth = stringResource(R.string.stats_period_month)
    val periodYear = stringResource(R.string.stats_period_year)
    val periodOptions = StatsPeriod.entries.map { period ->
        when (period) {
            StatsPeriod.WEEK -> periodWeek
            StatsPeriod.MONTH -> periodMonth
            StatsPeriod.YEAR -> periodYear
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
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
                    text = stringResource(R.string.stats_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                SegmentedControl(
                    options = periodOptions,
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
                overrideLabel = overrideLabel,
                unitPreference = state.unitPreference,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        if (!isAdFree) {
            item {
                InlineBannerAd(
                    state = bannerAdState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                )
            }
        }

        item {
            WeeklyBarChart(
                period = state.selectedPeriod,
                pages = state.pages,
                onPageChanged = onChartPageChanged,
                unitPreference = state.unitPreference,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        item {
            MiniStatsCards(
                drinkStreakDays = state.drinkStreakDays,
                bestDrinkStreakDays = state.bestDrinkStreakDays,
                goalStreakDays = state.goalStreakDays,
                bestGoalStreakDays = state.bestGoalStreakDays,
                personalBestMl = state.personalBestMl,
                unitPreference = state.unitPreference,
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
    avgMl = 1850f,
    avgTrendPercent = 12,
    isTrendPositive = true,
    showTrend = true,
    goalStreakDays = 4,
    bestGoalStreakDays = 12,
    drinkStreakDays = 7,
    bestDrinkStreakDays = 20,
    personalBestMl = 2950f,
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
            isAdFree = false,
            onPeriodSelected = {},
            onChartPageChanged = {},
            onCalendarPrevious = {},
            onCalendarNext = {},
            onCalendarViewModeToggle = {},
        )
    }
}
