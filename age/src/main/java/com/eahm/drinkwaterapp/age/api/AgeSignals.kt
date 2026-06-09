package com.eahm.drinkwaterapp.age.api

/**
 * The resolved age signal we act on. Used **only** for age-appropriate ad configuration and legal
 * compliance — never for analytics, profiling, or ad targeting.
 */
data class AgeSignals(
    val adTreatment: AdTreatment,
    val source: AgeSource,
    /** Lower bound of the reported age range, when known. Informational. */
    val ageLower: Int? = null,
    /** Upper bound of the reported age range, when known. Informational. */
    val ageUpper: Int? = null,
    /** Verification status when sourced from the API; null for declared values. */
    val status: AgeStatus? = null,
) {
    val isMinor: Boolean get() = adTreatment.isMinor

    companion object {
        /** Builds a signal from an API response. */
        fun fromApi(
            status: AgeStatus,
            ageLower: Int?,
            ageUpper: Int?,
        ): AgeSignals = AgeSignals(
            adTreatment = AdTreatment.fromSignal(status, ageLower, ageUpper),
            source = AgeSource.API,
            ageLower = ageLower,
            ageUpper = ageUpper,
            status = status,
        )

        /** Builds a signal from a user-declared age (derived from the birth-year input). */
        fun fromDeclaredAge(age: Int): AgeSignals = AgeSignals(
            adTreatment = AdTreatment.fromAge(age),
            source = AgeSource.DECLARED,
            ageLower = age,
            ageUpper = age,
            status = null,
        )
    }
}
