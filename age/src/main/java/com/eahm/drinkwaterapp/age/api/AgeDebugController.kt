package com.eahm.drinkwaterapp.age.api

/**
 * Developer-menu seam for pinning a fixed Play Age Signals response. The pinned response is
 * persisted and applied on the next app startup. **Only functional on debug builds** — on release
 * [isAvailable] is false and [AgeSignalsProvider] always calls the real API.
 */
interface AgeDebugController {

    /** True only on debuggable builds. When false, overriding the API is impossible. */
    val isAvailable: Boolean

    suspend fun isOverrideEnabled(): Boolean
    suspend fun setOverrideEnabled(enabled: Boolean)

    suspend fun getSelectedResponse(): AgeSimulatedResponse?
    suspend fun setSelectedResponse(response: AgeSimulatedResponse?)
}
