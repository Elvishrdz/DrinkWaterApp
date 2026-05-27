package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingStats {

    /**
     * Recorded each time the user switches the period tab in the stats screen.
     * Reveals which time-range users find most useful.
     *
     * @param period  The selected period. Use the PERIOD_* constants below.
     */
    fun trackStatsPeriodSelected(period: String)

    /**
     * Recorded when the user taps a bar in the chart to reveal that day's water amount popup.
     * High frequency = users actively explore daily data; consider surfacing it more prominently.
     */
    fun trackStatsBarTapped()

    /**
     * Recorded when the user taps the personal best card to reveal the achievement date tooltip.
     * Indicates interest in milestone details.
     */
    fun trackStatsPersonalBestTapped()

    companion object {
        const val EVENT_STATS_PERIOD_SELECTED = "stats_period_selected"
        const val EVENT_STATS_BAR_TAPPED = "stats_bar_tapped"
        const val EVENT_STATS_PERSONAL_BEST_TAPPED = "stats_personal_best_tapped"

        const val PERIOD_WEEK = "week"
        const val PERIOD_MONTH = "month"
        const val PERIOD_YEAR = "year"
    }
}
