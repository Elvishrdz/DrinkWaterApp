package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig

/**
 * Local feature flags for the app.
 * TODO: Replace local values with Firebase Remote Config when connected.
 */
@Suppress("MayBeConstant", "KotlinConstantConditions")
object FeatureFlags {
    val requestFeatureEnabled: Boolean = BuildConfig.FEATURE_REQUEST_FEATURE
    val streakFeatureEnabled: Boolean = BuildConfig.FEATURE_STREAKS
    val cloudBackupEnabled: Boolean = BuildConfig.FEATURE_CLOUD_BACKUP

    // Dropbox backup is not fully tested yet — keep it dev-only until validated.
    val dropboxBackupEnabled: Boolean = BuildConfig.IS_DEV
}
