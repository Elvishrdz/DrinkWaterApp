package com.eahm.drinkwaterapp.analytics.tracking

import android.app.Application
import android.content.Context
import com.eahm.drinkwaterapp.analytics.BuildConfig
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_DRINK_RECORDED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_PRESET_BOTTOM_SHEET_OPENED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingDrink.Companion.EVENT_PRESET_CREATED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingNavigation.Companion.EVENT_SCREEN_VIEWED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_ABANDONED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_COMPLETED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_STARTED
import com.eahm.drinkwaterapp.analytics.tracking.TrackingOnboarding.Companion.TRACK_EVENT_ONBOARDING_TIME
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService.Companion.EVENT_TRACKING_OPTED_OUT
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService.Companion.OptOutSource
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

internal class MixpanelTrackingService(
    application: Application,
) : TrackingService {

    private val token = BuildConfig.MIXPANEL_TOKEN
    private val trackAutomaticEvents = false

    // App-level consent flag — persisted across sessions. Defaults to true so tracking
    // is active from the very first launch and we can observe whether users opt out.
    // This will be explained in the privacy policy. It is required to identify when users dont want
    // to enable tracking as part of the app feature.
    private val prefs = application.getSharedPreferences("analytics_prefs", Context.MODE_PRIVATE)
    private var consentGranted: Boolean
        get() = prefs.getBoolean("tracking_consent", true)
        set(value) {
            prefs.edit().putBoolean("tracking_consent", value).apply()
        }

    // Mixpanel SDK is always opted-in at the SDK level.
    // Consent is enforced by our own sendEvent() wrapper so we never lose the
    // tracking_opted_out event that fires just before the flag is revoked.
    private val mixpanel = MixpanelAPI.getInstance(
        application,
        token,
        false,
        trackAutomaticEvents,
    ).apply {
        flushBatchSize = 70
        setServerURL("https://api-eu.mixpanel.com")
    }

    override fun startTracking() {
        consentGranted = true
    }

    override fun stopTracking(source: OptOutSource) {
        // Send the opt-out event directly — intentional bypass of sendEvent so this
        // last event is always delivered regardless of the current consent state.
        mixpanel.track(EVENT_TRACKING_OPTED_OUT, JSONObject().apply {
            put("source", source.name)
        })
        mixpanel.flush()
        consentGranted = false
    }

    override fun isTracking(): Boolean = consentGranted

    override fun flush() {
        mixpanel.flush()
    }

    override fun getDistinctId(): String? = mixpanel.distinctId?.takeIf { it.isNotBlank() }

    // All events must route through here. Returns early when consent is not granted.
    private fun sendEvent(event: String, props: JSONObject? = null) {
        if (!consentGranted) return
        if (props != null) mixpanel.track(event, props) else mixpanel.track(event)
    }

    private fun sendTimeEvent(event: String) {
        if (!consentGranted) return
        mixpanel.timeEvent(event)
    }


    override fun trackTestEvent(parameter: String) {
        sendEvent("test_event", JSONObject().apply {
            put("parameter", parameter)
        })
    }

    override fun startTrackTimeOnboardingTime() {
        sendTimeEvent(TRACK_EVENT_ONBOARDING_TIME)
    }

    override fun stopTrackTimeOnboardingTime() {
        sendEvent(TRACK_EVENT_ONBOARDING_TIME)
    }

    override fun trackOnboardingStarted() {
        sendEvent(TRACK_EVENT_ONBOARDING_STARTED)
    }

    override fun trackOnboardingAbandoned(stepName: String) {
        sendEvent(TRACK_EVENT_ONBOARDING_ABANDONED, JSONObject().apply { put("step", stepName) })
    }

    override fun trackOnboardingCompleted() {
        sendEvent(TRACK_EVENT_ONBOARDING_COMPLETED)
    }

    override fun trackDrinkRecorded(source: String, amountMl: Float) {
        sendEvent(EVENT_DRINK_RECORDED, JSONObject().apply {
            put("source", source)
            put("amount_ml", amountMl)
        })
    }

    override fun trackPresetCreated(amountMl: Float, totalPresets: Int) {
        sendEvent(EVENT_PRESET_CREATED, JSONObject().apply {
            put("amount_ml", amountMl)
            put("total_presets", totalPresets)
        })
    }

    override fun trackPresetBottomSheetOpened(presetCount: Int) {
        sendEvent(EVENT_PRESET_BOTTOM_SHEET_OPENED, JSONObject().apply {
            put("preset_count", presetCount)
        })
    }

    override fun trackScreenViewed(screen: String) {
        sendEvent(EVENT_SCREEN_VIEWED, JSONObject().apply {
            put("screen", screen)
        })
    }
}
