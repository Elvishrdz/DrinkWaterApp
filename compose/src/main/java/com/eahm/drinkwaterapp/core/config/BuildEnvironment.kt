package com.eahm.drinkwaterapp.core.config

import com.eahm.drinkwaterapp.BuildConfig

/**
 * Exposes the current build environment so the app can adjust behavior
 * at runtime without scattering BuildConfig references across the codebase.
 *
 * ## Environments
 *
 * ### debug
 * Used by developers on local machines.
 * - All feature flags and dev tooling enabled.
 * - The DEV tab is visible in the home screen.
 * - Full logging enabled.
 * - Published to: nowhere (never uploaded to the Play Store).
 *
 * ### staging
 * Beta / alpha builds for the Internal and Open Testing tracks.
 * - Beta feature flags enabled; **dev tools are hidden**.
 * - Logging enabled so testers can capture verbose output.
 * - Published to: Google Play Internal Testing → Open Testing tracks.
 *
 * ### qa
 * Internal QA builds for semi-production validation by the team.
 * - Beta feature flags enabled; **dev tools visible** (same as debug).
 * - Debug-signed — any team member can install via ADB or direct APK.
 * - Uses production API keys to validate the full purchase flow.
 * - Published to: nowhere (distributed directly to the QA team).
 *
 * ### production
 * The build that reaches end users.
 * - Only stable, fully tested features enabled.
 * - No logging.
 * - Published to: Google Play Closed Testing → Open Testing → Production.
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
 * if (BuildEnvironment.isDev || BuildEnvironment.isQa) { ... }
 * ```
 */
object BuildEnvironment {
    /** True only when running the `debug` build on a developer machine. */
    val isDev: Boolean = BuildConfig.IS_DEV

    /** True when running the `staging` build (Internal / Open Testing track). */
    val isStaging: Boolean = BuildConfig.IS_STAGING

    /**
     * True when running the `qa` build for internal team validation.
     * Dev tooling is visible but the build is never published publicly.
     */
    val isQa: Boolean = BuildConfig.IS_QA

    /**
     * True when running the `release` build distributed to end users.
     * All other environments (debug, staging, qa) return false.
     */
    val isProduction: Boolean = !BuildConfig.IS_DEV && !BuildConfig.IS_STAGING && !BuildConfig.IS_QA

    /** True in `debug`, `staging`, and `qa`. False in `release`. */
    val logsEnabled: Boolean = BuildConfig.LOGS_ENABLED
}
