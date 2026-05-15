package com.eahm.drinkwaterapp.analytics.tracking

import android.app.Application
import com.eahm.drinkwaterapp.analytics.BuildConfig
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_DRINK_RECORDED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_PRESET_BOTTOM_SHEET_OPENED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_PRESET_CREATED
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

    override fun stopTracking() {
        mixpanel.optOutTracking()
    }

    override fun isTracking(): Boolean = !mixpanel.hasOptedOutTracking()

    override fun trackTestEvent(parameter: String) {
        val props = JSONObject().apply {
            put("parameter", parameter)
        }
        mixpanel.track("test_event", props)
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

    override fun trackDrinkRecorded(source: String, amountMl: Float) {
        val props = JSONObject().apply {
            put("source", source)
            put("amount_ml", amountMl)
        }
        mixpanel.track(EVENT_DRINK_RECORDED, props)
    }

    override fun trackPresetCreated(amountMl: Float, totalPresets: Int) {
        val props = JSONObject().apply {
            put("amount_ml", amountMl)
            put("total_presets", totalPresets)
        }
        mixpanel.track(EVENT_PRESET_CREATED, props)
    }

    override fun trackPresetBottomSheetOpened(presetCount: Int) {
        val props = JSONObject().apply {
            put("preset_count", presetCount)
        }
        mixpanel.track(EVENT_PRESET_BOTTOM_SHEET_OPENED, props)
    }

    override fun clear() {
        mixpanel?.flush()
    }
}
