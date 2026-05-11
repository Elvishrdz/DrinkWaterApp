package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig

/**
 * Local feature flags for the app.
 * TODO: Replace local values with Firebase Remote Config when connected.
 */
object FeatureFlags {
    val requestFeatureEnabled: Boolean = BuildConfig.FEATURE_REQUEST_FEATURE
}
