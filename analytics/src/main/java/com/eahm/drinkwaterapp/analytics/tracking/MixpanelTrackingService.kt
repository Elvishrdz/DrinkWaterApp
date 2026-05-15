package com.eahm.drinkwaterapp.analytics.tracking

import android.app.Application
import com.eahm.drinkwaterapp.analytics.BuildConfig
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_ABANDONED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_COMPLETED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_STARTED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_TIME
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

internal class MixpanelTrackingService(
    application: Application,
) : TrackingService {

    private val token = BuildConfig.MIXPANEL_TOKEN
    private val trackAutomaticEvents = false
    private val optOutTrackingDefault = true


    private val mixpanel = MixpanelAPI.getInstance(
        application,
        token,
        optOutTrackingDefault,
        trackAutomaticEvents,
    ).apply {
        // By default, Mixpanel will flush events immediately if a batch reaches 50 events
        flushBatchSize = 70
    }

    override fun startTracking() {
        // set the Mixpanel instance to send data using Mixpanel's EU domain
        mixpanel.setServerURL("https://api-eu.mixpanel.com");

        // opt user back into tracking
        mixpanel.optInTracking()
    }

    override fun startTrackTimeOnboardingTime() {
        mixpanel.timeEvent(TRACK_EVENT_ONBOARDING_TIME)
    }

    override fun stopTrackTimeOnboardingTime() {
        mixpanel.track(TRACK_EVENT_ONBOARDING_TIME)
    }

    override fun trackOnboardingStarted() {
        mixpanel.track(TRACK_EVENT_ONBOARDING_STARTED)
    }

    override fun trackOnboardingAbandoned(stepName: String) {
        //Create event properties object
        val props = JSONObject().apply {
            put("step", stepName)
        }
        mixpanel.track(TRACK_EVENT_ONBOARDING_ABANDONED, props)
    }

    override fun trackOnboardingCompleted() {
        mixpanel.track(TRACK_EVENT_ONBOARDING_COMPLETED)
    }

    override fun clear() {
        mixpanel?.flush()
    }
}
