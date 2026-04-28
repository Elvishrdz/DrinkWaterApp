package com.eahm.drinkwaterapp.feature.stats

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class StatsViewModel : ViewModel() {

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    fun onPeriodSelected(period: StatsPeriod) {
        // Reset to page 0 (most recent) when switching period types
        _state.update { it.copy(selectedPeriod = period, chartPage = 0) }
        // TODO: reload pages for the selected period from the data layer
    }

    fun onChartPageChanged(page: Int) {
        val pages = _state.value.pages
        if (page !in pages.indices) return
        val summary = computeSummary(pages, page, _state.value.goalMl)
        _state.update {
            it.copy(
                chartPage = page,
                avgMl = summary.avgMl,
                avgTrendPercent = summary.trendPercent,
                isTrendPositive = summary.isTrendPositive,
            )
        }
    }

    private data class Summary(
        val avgMl: Int,
        val trendPercent: Int,
        val isTrendPositive: Boolean,
    )

    /**
     * Computes average intake and week-over-week trend for [pageIndex].
     * Only counts days with amountMl > 0 (logged days).
     * Trend compares to the next older page (pageIndex + 1).
     */
    private fun computeSummary(pages: List<PeriodPage>, pageIndex: Int, goal: Int): Summary {
        val amounts = pages[pageIndex].entries.map { it.amountMl }.filter { it > 0 }
        val avg = if (amounts.isNotEmpty()) amounts.average().toInt() else 0

        val prevAmounts = pages.getOrNull(pageIndex + 1)
            ?.entries?.map { it.amountMl }?.filter { it > 0 }
        val prevAvg = if (!prevAmounts.isNullOrEmpty()) prevAmounts.average().toInt() else avg

        val trendPercent = if (prevAvg > 0) {
            ((avg - prevAvg).toDouble() / prevAvg * 100).toInt()
        } else 0

        return Summary(
            avgMl = avg,
            trendPercent = kotlin.math.abs(trendPercent),
            isTrendPositive = trendPercent >= 0,
        )
    }

    /**
     * Builds 8 weeks of mock data.
     * Today = Tuesday Apr 28, 2026 → week index 0, day index 1 (Mon=0, Tue=1 …).
     *
     * Replace this with real data layer calls once functionality is connected.
     */
    private fun buildInitialState(): StatsState {
        val goal = 2400
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        // Each inner list: [Mon, Tue, Wed, Thu, Fri, Sat, Sun] in mL.
        // 0 = no data (future day or missing log).
        val weekAmounts = listOf(
            /* Week 0 – Apr 27 – May 3  (today = Tue, index 1) */
            listOf(2200, 1500, 0, 0, 0, 0, 0),
            /* Week 1 – Apr 20 – Apr 26 */
            listOf(2300, 2200, 2550, 2100, 2400, 1900, 2600),
            /* Week 2 – Apr 13 – Apr 19 */
            listOf(1900, 2100, 2300, 2400, 2100, 2200, 2050),
            /* Week 3 – Apr 6  – Apr 12 */
            listOf(2100, 2400, 2200, 1800, 2300, 2500, 2100),
            /* Week 4 – Mar 30 – Apr 5  */
            listOf(2000, 2200, 1700, 2400, 2100, 2300, 1900),
            /* Week 5 – Mar 23 – Mar 29 */
            listOf(1800, 2100, 2400, 2000, 1900, 2200, 2100),
            /* Week 6 – Mar 16 – Mar 22 */
            listOf(1700, 1900, 2100, 2300, 2000, 2100, 1800),
            /* Week 7 – Mar 9  – Mar 15 */
            listOf(1900, 2000, 1800, 2100, 2200, 2000, 1700),
        )

        val weekRanges = listOf(
            "Apr 27 – May 3",
            "Apr 20 – Apr 26",
            "Apr 13 – Apr 19",
            "Apr 6 – Apr 12",
            "Mar 30 – Apr 5",
            "Mar 23 – Mar 29",
            "Mar 16 – Mar 22",
            "Mar 9 – Mar 15",
        )

        val pages = weekAmounts.mapIndexed { weekIndex, amounts ->
            PeriodPage(
                rangeLabel = weekRanges[weekIndex],
                entries = amounts.mapIndexed { dayIndex, amount ->
                    DayEntry(
                        shortLabel = dayLabels[dayIndex],
                        amountMl = amount,
                        isToday = weekIndex == 0 && dayIndex == 1, // Tue Apr 28
                    )
                }
            )
        }

        val personalBest = weekAmounts.flatten().maxOrNull() ?: 0
        val initialSummary = computeSummary(pages, 0, goal)

        return StatsState(
            pages = pages,
            goalMl = goal,
            avgMl = initialSummary.avgMl,
            avgTrendPercent = initialSummary.trendPercent,
            isTrendPositive = initialSummary.isTrendPositive,
            streakDays = 4,
            personalBestMl = personalBest,
        )
    }
}
