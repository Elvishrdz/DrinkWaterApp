package com.eahm.drinkwaterapp.age.api

/**
 * Developer-menu seam for simulating Play Age Signals API responses without a real device/account.
 * In production the override is always null, so [AgeSignalsProvider] delegates to the real Play API.
 */
interface AgeDebugController {

    /** The currently simulated resolution, or null when the real API is used. */
    val override: AgeResolution?

    /** Forces [AgeSignalsProvider.resolve] to return [resolution]; pass null to use the real API. */
    fun simulate(resolution: AgeResolution?)
}
