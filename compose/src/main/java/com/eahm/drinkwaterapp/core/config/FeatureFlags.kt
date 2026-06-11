package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig

/**
 * Local feature flags for the app.
 */
@Suppress("MayBeConstant")
object FeatureFlags {
    val requestFeatureEnabled: Boolean = BuildConfig.FEATURE_REQUEST_FEATURE
    val streakFeatureEnabled: Boolean = BuildConfig.FEATURE_STREAKS
    val dropboxBackupEnabled: Boolean = BuildConfig.IS_DEV
}
