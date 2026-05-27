package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingNotification {

    /**
     * Recorded when the user taps the Skip action button in the reminder notification.
     * High frequency = reminders are too frequent or poorly timed; consider adjusting defaults.
     */
    fun trackNotificationSkipTapped()

    /**
     * Recorded when the user taps the drink action button (+amount) in the reminder notification.
     * This is the primary success action — compare with skip to measure reminder effectiveness.
     */
    fun trackNotificationDrinkTapped()

    /**
     * Recorded when the user taps the notification body to open the app.
     * Indicates the notification caught attention without the user acting on the action buttons.
     */
    fun trackNotificationOpened()

    companion object {
        const val EVENT_NOTIFICATION_SKIP_TAPPED = "notification_skip_tapped"
        const val EVENT_NOTIFICATION_DRINK_TAPPED = "notification_drink_tapped"
        const val EVENT_NOTIFICATION_OPENED = "notification_opened"
    }
}
