package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingSettings {

    /** User saved a new daily water goal. [goalMl] identifies the most popular targets. */
    fun trackSettingsDailyGoalChanged(goalMl: Float)

    /** User opened the Remove Ads purchase sheet. */
    fun trackSettingsRemoveAdsTapped()

    /** User changed the volume display unit. [unit] is the selected UnitPreference name. */
    fun trackSettingsVolumeUnitChanged(unit: String)

    /**
     * User tapped "Go to Settings" inside the reminder-type dialog (exact alarm permission).
     * Indicates intent to switch alarm precision.
     */
    fun trackSettingsReminderTypeOpened()

    /**
     * User tapped "Go to Settings" inside the disable-notifications confirmation dialog.
     * Indicates the user actively wants to stop receiving reminders.
     */
    fun trackSettingsNotificationsDisabled()

    /** User saved a new quick-drink (notification action) amount. [amountMl] is the chosen value. */
    fun trackSettingsQuickDrinkChanged(amountMl: Float)

    /** User saved a new reminder start time. */
    fun trackSettingsStartTimeChanged(hour: Int, minute: Int)

    /** User saved a new reminder end time. */
    fun trackSettingsEndTimeChanged(hour: Int, minute: Int)

    /**
     * User tapped "Go to Settings" inside the goal-reached overlay instructions dialog.
     * Only fires when the user navigates to system settings, not on switch toggle.
     */
    fun trackSettingsGoalReachedOpened()

    /** User tapped the "Share with friends" option. */
    fun trackSettingsShareTapped()

    /** User tapped the "Rate the app" option. */
    fun trackSettingsRateAppTapped()

    /** User tapped "Feature request". */
    fun trackSettingsFeatureRequestTapped()

    /** User tapped "Report a problem". */
    fun trackSettingsReportProblemTapped()

    /**
     * User confirmed "Reset all data" after the 5-second countdown.
     * This is a destructive action; low frequency is expected and healthy.
     */
    fun trackSettingsResetAllDataConfirmed()

    companion object {
        const val EVENT_SETTINGS_DAILY_GOAL_CHANGED = "settings_daily_goal_changed"
        const val EVENT_SETTINGS_REMOVE_ADS_TAPPED = "settings_remove_ads_tapped"
        const val EVENT_SETTINGS_VOLUME_UNIT_CHANGED = "settings_volume_unit_changed"
        const val EVENT_SETTINGS_REMINDER_TYPE_OPENED = "settings_reminder_type_opened"
        const val EVENT_SETTINGS_NOTIFICATIONS_DISABLED = "settings_notifications_disabled"
        const val EVENT_SETTINGS_QUICK_DRINK_CHANGED = "settings_quick_drink_changed"
        const val EVENT_SETTINGS_START_TIME_CHANGED = "settings_start_time_changed"
        const val EVENT_SETTINGS_END_TIME_CHANGED = "settings_end_time_changed"
        const val EVENT_SETTINGS_GOAL_REACHED_OPENED = "settings_goal_reached_opened"
        const val EVENT_SETTINGS_SHARE_TAPPED = "settings_share_tapped"
        const val EVENT_SETTINGS_RATE_APP_TAPPED = "settings_rate_app_tapped"
        const val EVENT_SETTINGS_FEATURE_REQUEST_TAPPED = "settings_feature_request_tapped"
        const val EVENT_SETTINGS_REPORT_PROBLEM_TAPPED = "settings_report_problem_tapped"
        const val EVENT_SETTINGS_RESET_ALL_DATA_CONFIRMED = "settings_reset_all_data_confirmed"
    }
}
