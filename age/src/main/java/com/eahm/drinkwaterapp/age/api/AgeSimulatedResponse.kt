package com.eahm.drinkwaterapp.age.api

/**
 * Fixed Play Age Signals responses that the developer menu can pin. When override is enabled (debug
 * builds only), the selected entry's [resolution] is returned by [AgeSignalsProvider] on the next
 * app startup instead of calling the real API.
 */
enum class AgeSimulatedResponse(
    val label: String,
    val resolution: AgeResolution,
) {
    COVERED_ADULT(
        label = "Covered · Adult (Verified 18+)",
        resolution = AgeResolution.Covered(
            AgeSignals.fromApi(
                AgeStatus.VERIFIED,
                ageLower = 18,
                ageUpper = null
            )
        ),
    ),
    COVERED_TEEN(
        label = "Covered · Teen (Declared 13–17)",
        resolution = AgeResolution.Covered(
            AgeSignals.fromApi(
                AgeStatus.DECLARED,
                ageLower = 13,
                ageUpper = 17
            )
        ),
    ),
    COVERED_CHILD(
        label = "Covered · Child (Supervised <13)",
        resolution = AgeResolution.Covered(
            AgeSignals.fromApi(
                AgeStatus.SUPERVISED,
                ageLower = 0,
                ageUpper = 12
            )
        ),
    ),
    COVERED_AGE_UNKNOWN(
        label = "Covered · Age unknown (UNKNOWN)",
        resolution = AgeResolution.CoveredAgeUnknown,
    ),
    NOT_COVERED(
        label = "Not covered (null)",
        resolution = AgeResolution.NotCovered,
    ),
    UNDETERMINED_NETWORK(
        label = "Undetermined · Network error",
        resolution = AgeResolution.Undetermined(AgeError.NETWORK_ERROR),
    ),
    UNDETERMINED_NOT_OWNED(
        label = "Undetermined · App not owned",
        resolution = AgeResolution.Undetermined(AgeError.APP_NOT_OWNED),
    ),

    /**
     * Simulates a first launch in a covered jurisdiction with no age on file — i.e. the bootstrap
     * age-collection screen should be shown. Selecting this also clears any stored age.
     */
    FIRST_LAUNCH_AGE_COLLECTION(
        label = "First launch · age collection",
        resolution = AgeResolution.CoveredAgeUnknown,
    ),
    ;

    /** True when picking this preset should also wipe stored age so the bootstrap screen reappears. */
    val clearsStoredAge: Boolean get() = this == FIRST_LAUNCH_AGE_COLLECTION
}
