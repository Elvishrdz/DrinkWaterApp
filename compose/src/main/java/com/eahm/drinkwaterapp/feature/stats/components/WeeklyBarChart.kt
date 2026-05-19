package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.stats.DayEntry
import com.eahm.drinkwaterapp.feature.stats.PeriodPage
import com.eahm.drinkwaterapp.feature.stats.StatsPeriod
import kotlinx.coroutines.launch

// Goal met: magenta gradient (design spec)
private val goalMetBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFB83A95), DwMagentaDark)
)

// Below goal: water gradient (design spec)
private val belowGoalBrush = Brush.verticalGradient(
    colors = listOf(DwWaterLight, DwWater)
)

/**
 * A horizontally paginated bar chart.
 *
 * Pagination model (chronological order):
 *   pages[0] = oldest period
 *   pages[N] = most recent period (current week / month / year)
 *
 * Swiping / navigating RIGHT → more recent dates (avanzar)
 * Swiping / navigating LEFT  → older dates (retroceder)
 *
 * @param pages          Ordered list of period data. pages[0] = oldest, pages[N] = most recent.
 *                       Each [DayEntry] carries its own [DayEntry.goalMl] for that specific day.
 * @param onPageChanged  Callback fired when the visible page changes.
 */
@Composable
fun WeeklyBarChart(
    period: StatsPeriod,
    pages: List<PeriodPage>,
    onPageChanged: (page: Int) -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    if (pages.isEmpty()) return

    val initialPage = pages.size - 1
    var currentPage by remember { mutableIntStateOf(initialPage) }
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pages.size },
    )
    val scope = rememberCoroutineScope()

    // Scroll to newest page whenever the list size changes (period switch)
    LaunchedEffect(pages.size) {
        pagerState.scrollToPage(pages.size - 1)
    }

    // Keep ViewModel in sync with swipe navigation
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
        onPageChanged(pagerState.currentPage)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .outlineBorderStyle()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        ChartHeader(
            period = period,
            rangeLabel = pages.getOrNull(pagerState.currentPage)?.rangeLabel ?: "",
            pagerState = pagerState,
            isToday = currentPage == initialPage,
            onNavigateToToday = {
                scope.launch {
                    pagerState.animateScrollToPage(initialPage)
                }
            },
            onNavigatePrev = {
                // ← Older: go to lower page index
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNavigateNext = {
                // → Newer: go to higher page index
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            pageSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { pageIndex ->
            val page = pages[pageIndex]
            if (page.isBeforeFirstRecord) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                ) {
                    Text(
                        text = stringResource(R.string.stats_chart_no_data),
                        fontSize = 13.sp,
                        color = DwInk300,
                        fontFamily = PlusJakartaSans,
                    )
                }
            } else {
                BarChartPage(
                    entries = page.entries,
                    isCurrentPage = pageIndex == pagerState.currentPage,
                    unitPreference = unitPreference,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChartLegend()
    }
}

@Composable
private fun ChartHeader(
    period: StatsPeriod,
    rangeLabel: String,
    pagerState: PagerState,
    isToday: Boolean,
    onNavigateToToday: () -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    val label = when (period) {
        StatsPeriod.WEEK, StatsPeriod.MONTH -> stringResource(R.string.stats_chart_daily_intake)
        StatsPeriod.YEAR -> stringResource(R.string.stats_chart_monthly_intake)
    }
    val todayLabel = stringResource(R.string.label_today)
    val prevPeriodDesc = stringResource(R.string.stats_chart_prev_period_desc)
    val nextPeriodDesc = stringResource(R.string.stats_chart_next_period_desc)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!isToday) {
            Text(
                text = todayLabel,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = DwWater,
                modifier = Modifier.clickable(
                    onClick = onNavigateToToday,
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // ← Older (lower page index)
        IconButton(
            onClick = onNavigatePrev,
            enabled = pagerState.currentPage > 0,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = prevPeriodDesc,
                tint = if (pagerState.currentPage > 0) DwInk400 else DwInk100,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { scaleX = -1f },
            )
        }

        Text(
            text = rangeLabel,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // → Newer (higher page index)
        IconButton(
            onClick = onNavigateNext,
            enabled = pagerState.currentPage < pagerState.pageCount - 1,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = nextPeriodDesc,
                tint = if (pagerState.currentPage < pagerState.pageCount - 1) DwInk400 else DwInk100,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun BarChartPage(
    entries: List<DayEntry>,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Close the tooltip whenever this page is swiped away
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) selectedIndex = null
    }

    // Monthly view: 28–31 bars. Show sparse labels to avoid vertical text stacking.
    // Show label only for day 1, multiples of 10 (10, 20, 30), and today.
    val isMonthly = entries.size in 28..31

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        entries.forEachIndexed { index, entry ->
            val dayNum = entry.shortLabel.toIntOrNull() ?: 0
            val showLabel = !isMonthly ||
                    entry.isToday ||
                    dayNum == 1 ||
                    dayNum % 10 == 0

            BarColumn(
                entry = entry,
                displayLabel = if (showLabel) entry.shortLabel else "",
                isMonthly = isMonthly,
                isSelected = selectedIndex == index,
                unitPreference = unitPreference,
                onClick = {
                    selectedIndex = if (selectedIndex == index) null else index
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BarColumn(
    entry: DayEntry,
    displayLabel: String,
    isMonthly: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }

    val fillRatio = if (entry.goalMl > 0 && entry.amountMl > 0) {
        (entry.amountMl.toFloat() / entry.goalMl).coerceIn(0f, 1f)
    } else 0f

    val animatedFill by animateFloatAsState(
        targetValue = if (triggered) fillRatio else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "bar_${entry.shortLabel}",
    )

    val goalMet = entry.amountMl >= entry.goalMl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DwInk50)
                .clickable { onClick() },
        ) {
            if (animatedFill > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedFill)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (goalMet) goalMetBrush else belowGoalBrush),
                )
            }

            // Popup tooltip shown when bar is pressed
            if (isSelected && entry.amountMl > 0) {
                Popup(alignment = Alignment.TopCenter) {
                    BarTooltip(
                        amountMl = entry.amountMl,
                        goalMl = entry.goalMl,
                        unitPreference = unitPreference
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = displayLabel,
            fontFamily = PlusJakartaSans,
            fontWeight = if (entry.isToday) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = if (isMonthly) 9.sp else 10.sp,
            color = if (entry.isToday) DwMagenta else DwInk400,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
private fun BarTooltip(
    amountMl: Int,
    goalMl: Int,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(DwInk900)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = amountMl.toFloat().formatVolume(unitPreference),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
        )
        Text(
            text = "/ ${goalMl.toFloat().formatVolume(unitPreference)}",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = DwInk300,
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LegendItem(brush = goalMetBrush, label = stringResource(R.string.stats_chart_goal_met))
        LegendItem(brush = belowGoalBrush, label = stringResource(R.string.stats_chart_below_goal))
    }
}

@Composable
private fun LegendItem(brush: Brush, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(brush),
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Preview uses chronological order: oldest first, newest last
private val previewPages = listOf(
    PeriodPage(
        rangeLabel = "Apr 20 – Apr 26",
        entries = listOf(
            DayEntry("Mon", 2300, goalMl = 2400), DayEntry("Tue", 2200, goalMl = 2400),
            DayEntry("Wed", 2550, goalMl = 2400), DayEntry("Thu", 2100, goalMl = 2400),
            DayEntry("Fri", 2400, goalMl = 2400), DayEntry("Sat", 1900, goalMl = 2400),
            DayEntry("Sun", 2600, goalMl = 2400),
        ),
    ),
    PeriodPage(
        rangeLabel = "Apr 27 – May 3",
        entries = listOf(
            DayEntry("Mon", 2200, goalMl = 2400),
            DayEntry("Tue", 1500, isToday = true, goalMl = 2400),
            DayEntry("Wed", 0, goalMl = 2400), DayEntry("Thu", 0, goalMl = 2400),
            DayEntry("Fri", 0, goalMl = 2400), DayEntry("Sat", 0, goalMl = 2400),
            DayEntry("Sun", 0, goalMl = 2400),
        ),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun WeeklyBarChartPreview() {
    DrinkWaterAppTheme {
        WeeklyBarChart(
            period = StatsPeriod.WEEK,
            pages = previewPages,
            onPageChanged = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
