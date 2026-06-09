package com.eahm.drinkwaterapp.age.internal

import com.eahm.drinkwaterapp.age.api.AgeDebugController
import com.eahm.drinkwaterapp.age.api.AgeDebugPreferences
import com.eahm.drinkwaterapp.age.api.AgeResolution
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.eahm.drinkwaterapp.age.api.AgeSimulatedResponse

/**
 * Wraps the real [AgeSignalsProvider] with a persisted, debug-only override. On debug builds with
 * the override enabled and a response pinned, [resolve] returns that fixed response (applied at the
 * next startup); otherwise it delegates to the real Play API. On release builds [isAvailable] is
 * false and the override is never consulted.
 */
internal class SwitchableAgeSignalsProvider(
    private val delegate: AgeSignalsProvider,
    private val debugPreferences: AgeDebugPreferences,
    override val isAvailable: Boolean,
) : AgeSignalsProvider, AgeDebugController {

    override suspend fun resolve(): AgeResolution {
        if (isAvailable && debugPreferences.isOverrideEnabled()) {
            selectedOrNull()?.let { return it.resolution }
        }
        return delegate.resolve()
    }

    override suspend fun isOverrideEnabled(): Boolean =
        isAvailable && debugPreferences.isOverrideEnabled()

    override suspend fun setOverrideEnabled(enabled: Boolean) {
        if (isAvailable) debugPreferences.setOverrideEnabled(enabled)
    }

    override suspend fun getSelectedResponse(): AgeSimulatedResponse? =
        if (isAvailable) selectedOrNull() else null

    override suspend fun setSelectedResponse(response: AgeSimulatedResponse?) {
        if (isAvailable) debugPreferences.setSelectedResponse(response?.name)
    }

    private suspend fun selectedOrNull(): AgeSimulatedResponse? =
        debugPreferences.getSelectedResponse()
            ?.let { name -> runCatching { AgeSimulatedResponse.valueOf(name) }.getOrNull() }
}
