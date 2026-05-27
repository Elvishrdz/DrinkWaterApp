package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingService
    : TrackingOnboarding,
    TrackingDrink,
    TrackingNavigation,
    TrackingHistory,
    TrackingStats,
    TrackingSettings,
    TrackingNotification {

    fun startTracking()

    fun stopTracking(source: OptOutSource)

    fun isTracking(): Boolean

    fun flush()

    fun getDistinctId(): String?

    fun trackTestEvent(parameter: String)

    companion object {

        enum class OptOutSource {
            Welcome,
            Settings,
            DevMenu,
        }

        // using \ to use the native field App Version String from mixpanel
        // https://docs.mixpanel.com/docs/data-structure/property-reference/default-properties
        @Suppress("CanConvertToMultiDollarString")
        const val SUPER_PROP_APP_VERSION = "\$app_version_string"

        const val EVENT_TRACKING_OPTED_OUT = "tracking_opted_out"
    }
}

interface TrackingOnboarding {

    fun startTrackTimeOnboardingTime()
    fun stopTrackTimeOnboardingTime()

    fun trackOnboardingStarted()
    fun trackOnboardingAbandoned(stepName: String)
    fun trackOnboardingCompleted()

    companion object {

        const val TRACK_EVENT_ONBOARDING_TIME = "onboarding_time"
        const val TRACK_EVENT_ONBOARDING_STARTED = "onboarding_started"
        const val TRACK_EVENT_ONBOARDING_ABANDONED = "onboarding_abandoned"
        const val TRACK_EVENT_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
