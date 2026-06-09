package com.eahm.drinkwaterapp.age.api

/**
 * Typed reason an age resolution could not complete, mapped from the Play SDK's
 * `AgeSignalsErrorCode`. [retryable] indicates whether retrying the call (e.g. with backoff or on a
 * later cold start) might succeed.
 */
enum class AgeError(val retryable: Boolean) {
    /** Play Store API unavailable / outdated; user can update Play Store. */
    API_NOT_AVAILABLE(retryable = true),

    /** No network connection. */
    NETWORK_ERROR(retryable = true),

    /** Play services/store missing, outdated, or cannot bind. */
    PLAY_UNAVAILABLE(retryable = true),

    /** Transient client/internal error. */
    TRANSIENT(retryable = true),

    /** App was not installed/updated by Google Play (sideloaded). Not retryable. */
    APP_NOT_OWNED(retryable = false),

    /** The bundled Age Signals SDK is too old. Not retryable without an app update. */
    SDK_VERSION_OUTDATED(retryable = false),

    /** Any other / unknown error. */
    UNKNOWN(retryable = false),
}
