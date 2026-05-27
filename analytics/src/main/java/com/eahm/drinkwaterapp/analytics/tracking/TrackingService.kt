package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingService
    : TrackingOnboarding,
    TrackingDrink,
    TrackingNavigation,
    TrackingHistory {

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
