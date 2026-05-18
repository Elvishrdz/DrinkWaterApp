package com.eahm.drinkwaterapp.core.presentation

/** User preference for how times are displayed throughout the app. */
enum class TimeFormatPreference {
    /** Follow the device locale (auto-detect 12h / 24h). */
    SYSTEM,

    /** Always show 12-hour clock with AM / PM. */
    HOUR_12,

    /** Always show 24-hour clock. */
    HOUR_24,
}
