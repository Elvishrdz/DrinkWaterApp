package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingHistory {

    /**
     * Recorded when the user taps the "edit amount" button on a history record.
     * High frequency = users actively correct their entries; consider making inline edits easier.
     */
    fun trackHistoryEditAmountTapped()

    /**
     * Recorded when the user taps the "edit time" button on a history record.
     * High frequency = time-stamping matters to users; low frequency = button may be dispensable.
     */
    fun trackHistoryEditTimeTapped()

    /**
     * Recorded when the user taps the "delete" button on a history record.
     * High frequency = users record entries by mistake; consider adding quick-undo.
     */
    fun trackHistoryDeleteTapped()

    /**
     * Recorded when the user changes the date filter on the history screen.
     * High frequency = browsing past days is a core behaviour; low = only today matters.
     */
    fun trackHistoryFilterChanged()

    companion object {
        const val EVENT_HISTORY_EDIT_AMOUNT_TAPPED = "history_edit_amount_tapped"
        const val EVENT_HISTORY_EDIT_TIME_TAPPED = "history_edit_time_tapped"
        const val EVENT_HISTORY_DELETE_TAPPED = "history_delete_tapped"
        const val EVENT_HISTORY_FILTER_CHANGED = "history_filter_changed"
    }
}
