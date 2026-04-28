package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.stats.DayEntry
import com.eahm.drinkwaterapp.feature.stats.PeriodPage
import kotlinx.coroutines.launch

private val barGradient = Brush.verticalGradient(
    colors = listOf(DwWaterLight, DwWaterDeep)
)

/**
 * A horizontally paginated bar chart.
 *
 * Each page corresponds to one period (e.g. a week). Users swipe LEFT to view
 * older periods, RIGHT to return to more recent ones.
 *
 * Pagination model:
 *   page 0 = most recent period (e.g. current week)
 *   page N = oldest period available
 *
 * @param pages          Ordered list of period data. pages[0] = most recent.
 * @param goalMl         Daily goal used to calculate bar fill ratios.
 * @param onPageChanged  Callback fired when the visible page changes.
 */
@Composable
fun WeeklyBarChart(
    pages: List<PeriodPage>,
    goalMl: Int,
    onPageChanged: (page: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pages.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // Keep ViewModel in sync with swipe navigation
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .outlineBorderStyle()
            .padding(16.dp)
    ) {
        ChartHeader(
            rangeLabel = pages[pagerState.currentPage].rangeLabel,
            goalMl = goalMl,
            pagerState = pagerState,
            onNavigatePrev = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onNavigateNext = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            BarChartPage(
                entries = pages[pageIndex].entries,
                goalMl = goalMl
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChartLegend()
    }
}

@Composable
private fun ChartHeader(
    rangeLabel: String,
    goalMl: Int,
    pagerState: PagerState,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Daily intake",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = DwInk900
        )
        Text(
            text = "goal $goalMl mL",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = DwWater
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onNavigatePrev,
            enabled = pagerState.currentPage < pagerState.pageCount - 1,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Previous period",
                tint = if (pagerState.currentPage < pagerState.pageCount - 1) DwInk400
                else DwInk100,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { scaleX = -1f } // mirror → acts as left chevron
            )
        }

        Text(
            text = rangeLabel,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = DwInk500
        )

        IconButton(
            onClick = onNavigateNext,
            enabled = pagerState.currentPage > 0,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = "Next period",
                tint = if (pagerState.currentPage > 0) DwInk400 else DwInk100,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun BarChartPage(
    entries: List<DayEntry>,
    goalMl: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        entries.forEach { entry ->
            BarColumn(
                entry = entry,
                goalMl = goalMl,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarColumn(
    entry: DayEntry,
    goalMl: Int,
    modifier: Modifier = Modifier,
) {
    // Animate fill from 0 whenever this composable enters composition (page appears)
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }

    val fillRatio = if (goalMl > 0 && entry.amountMl > 0) {
        (entry.amountMl.toFloat() / goalMl).coerceIn(0f, 1f)
    } else 0f

    val animatedFill by animateFloatAsState(
        targetValue = if (triggered) fillRatio else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "bar_${entry.shortLabel}"
    )

    val goalMet = entry.amountMl >= goalMl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(DwInk100)
        ) {
            if (animatedFill > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedFill)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (goalMet) barGradient else Brush.linearGradient(
                                listOf(
                                    DwWater,
                                    DwWater
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.shortLabel,
            fontFamily = PlusJakartaSans,
            fontWeight = if (entry.isToday) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 10.sp,
            color = if (entry.isToday) DwMagenta else DwInk400
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LegendItem(color = DwWater, label = "Goal met")
        LegendItem(color = DwInk100, label = "Below goal")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = DwInk500
        )
    }
}

private val previewPages = listOf(
    PeriodPage(
        rangeLabel = "Apr 27 – May 3",
        entries = listOf(
            DayEntry("Mon", 2200), DayEntry("Tue", 1500, isToday = true),
            DayEntry("Wed", 0), DayEntry("Thu", 0),
            DayEntry("Fri", 0), DayEntry("Sat", 0), DayEntry("Sun", 0),
        )
    ),
    PeriodPage(
        rangeLabel = "Apr 20 – Apr 26",
        entries = listOf(
            DayEntry("Mon", 2300), DayEntry("Tue", 2200),
            DayEntry("Wed", 2550), DayEntry("Thu", 2100),
            DayEntry("Fri", 2400), DayEntry("Sat", 1900), DayEntry("Sun", 2600),
        )
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun WeeklyBarChartPreview() {
    DrinkWaterAppTheme {
        WeeklyBarChart(
            pages = previewPages,
            goalMl = 2400,
            onPageChanged = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
