package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingReminders {

    /**
     * User toggled a reminder active day. [isoDay] is ISO-8601 (1=Monday … 7=Sunday) and
     * [enabled] is the new state of that day. [activeIsoDays] is the resulting configuration
     * after the toggle, so dashboards can answer: which day is disabled the most, and which
     * active-days configuration is the most common (candidate for the future default).
     */
    fun trackReminderDayToggled(
        isoDay: Int,
        enabled: Boolean,
        activeIsoDays: Set<Int>,
        source: ReminderDaysSource,
    )

    /** User tapped "enable all days" to restore the full week. */
    fun trackReminderAllDaysEnabled(source: ReminderDaysSource)

    companion object {

        /** Where the active-days toggle was used — the Reminders screen or the Settings row. */
        enum class ReminderDaysSource {
            Reminders,
            Settings,
        }

        const val EVENT_REMINDER_DAY_TOGGLED = "reminder_day_toggled"
        const val EVENT_REMINDER_ALL_DAYS_ENABLED = "reminder_all_days_enabled"
    }
}
