package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig

/**
 * Exposes the current build environment so the app can adjust behavior
 * at runtime without scattering BuildConfig references across the codebase.
 *
 * ## Environments
 *
 * ### dev
 * Used by developers on local machines.
 * - All feature flags are enabled.
 * - The DEV tab is visible in the home screen.
 * - Full logging enabled.
 * - Published to: nowhere (never uploaded to the Play Store).
 *
 * ### staging
 * Used for QA and internal validation before a release.
 * - Beta feature flags are enabled, dev tools are hidden.
 * - Logging enabled so QA can capture verbose output.
 * - Published to: Google Play Internal Testing track.
 *
 * ### production
 * The build that reaches end users.
 * - Only stable, fully tested features are enabled.
 * - No logging.
 * - Published to: Google Play Closed Testing → Open Testing → Production track.
 *
 * ## Usage
 * Prefer checking [FeatureFlags] over checking the environment directly.
 * Use [BuildEnvironment] only when behavior needs to change based on the
 * environment itself, not on a specific feature (e.g. showing dev tooling).
 *
 * ```kotlin
 * // Prefer this
 * if (FeatureFlags.requestFeatureEnabled) { ... }
 *
 * // Over this
 * if (BuildEnvironment.isDev || BuildEnvironment.isStaging) { ... }
 * ```
 */
object BuildEnvironment {
    /** True only when running the `dev` flavor on a developer machine. */
    val isDev: Boolean = BuildConfig.IS_DEV

    /** True when running the `staging` flavor, used for QA and internal testing. */
    val isStaging: Boolean = BuildConfig.IS_STAGING

    /** True when running the `production` flavor distributed to end users. */
    val isProduction: Boolean = !BuildConfig.IS_DEV && !BuildConfig.IS_STAGING

    /** True in `dev` and `staging`. False in `production`. */
    val logsEnabled: Boolean = BuildConfig.LOGS_ENABLED
}
