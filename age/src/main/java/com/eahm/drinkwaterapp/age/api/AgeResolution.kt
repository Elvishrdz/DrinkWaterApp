package com.eahm.drinkwaterapp.age.api

/**
 * Outcome of a single Play Age Signals API call. Crucially distinguishes **jurisdiction**, because
 * age-verification law (Texas SB 2420 today; more states later) only applies to covered users — the
 * API tells us this via its status (or lack of one).
 */
sealed interface AgeResolution {

    /** Covered jurisdiction with a usable signal. Use [signals]. */
    data class Covered(val signals: AgeSignals) : AgeResolution

    /**
     * Covered jurisdiction, but the user's age is unknown (API status `UNKNOWN`). This is the only
     * API outcome that may warrant the one-time bootstrap age screen.
     */
    data object CoveredAgeUnknown : AgeResolution

    /**
     * The user is **not** in a covered region (API status `null`). No screen, standard ads. The
     * vast majority of users worldwide.
     */
    data object NotCovered : AgeResolution

    /**
     * The API could not be reached (network/service/transient/non-retryable error), so we have
     * **neither** a status nor a definitive "not covered". Jurisdiction is unknown.
     */
    data class Undetermined(val reason: AgeError) : AgeResolution
}
