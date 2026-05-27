package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingNavigation {

    /**
     * Recorded each time the user navigates to a main screen from the bottom navigation bar.
     * Use this event to measure which screens are most frequently visited.
     *
     * @param screen  The destination screen. Use the SCREEN_* constants below.
     */
    fun trackScreenViewed(screen: String)

    companion object {
        const val EVENT_SCREEN_VIEWED = "screen_viewed"

        const val SCREEN_TODAY = "today"
        const val SCREEN_HISTORY = "history"
        const val SCREEN_STATS = "stats"
        const val SCREEN_SETTINGS = "settings"
    }
}
