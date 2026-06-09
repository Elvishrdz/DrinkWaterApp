package com.eahm.drinkwaterapp.age.api

/**
 * Ad-serving treatment bracket derived from an age signal. Ad-compliance law has two distinct
 * thresholds, so a binary minor/adult flag is insufficient:
 *
 * - [CHILD_DIRECTED] (under 13) → AdMob TFCD (`setTagForChildDirectedTreatment`), COPPA.
 * - [UNDER_AGE_OF_CONSENT] (13–17) → AdMob TFUA (`setTagForUnderAgeOfConsent`).
 * - [STANDARD] (18+ or unconstrained) → no minor tag.
 */
enum class AdTreatment {
    CHILD_DIRECTED,
    UNDER_AGE_OF_CONSENT,
    STANDARD,
    ;

    val isMinor: Boolean get() = this != STANDARD

    companion object {
        /**
         * Derives the bracket from an age range and/or [AgeStatus]. Aligned to Play's default age
         * ranges (0–12 / 13–15 / 16–17 / 18+).
         *
         * Conservative when ambiguous: a supervised account with no usable upper bound is treated
         * as [UNDER_AGE_OF_CONSENT] rather than [STANDARD].
         */
        fun fromSignal(
            status: AgeStatus?,
            ageLower: Int?,
            ageUpper: Int?,
        ): AdTreatment = when {
            ageUpper != null && ageUpper < CHILD_MAX_AGE -> CHILD_DIRECTED
            ageUpper != null && ageUpper < ADULT_MIN_AGE -> UNDER_AGE_OF_CONSENT
            ageLower != null && ageLower >= ADULT_MIN_AGE -> STANDARD
            status?.isSupervisedMinor == true -> UNDER_AGE_OF_CONSENT
            else -> STANDARD
        }

        /** Derives the bracket from a concrete age (e.g. from a declared birth year). */
        fun fromAge(age: Int): AdTreatment = when {
            age < CHILD_MAX_AGE -> CHILD_DIRECTED
            age < ADULT_MIN_AGE -> UNDER_AGE_OF_CONSENT
            else -> STANDARD
        }

        private const val CHILD_MAX_AGE = 13
        private const val ADULT_MIN_AGE = 18

        @Suppress("Indentation")
        private val AgeStatus.isSupervisedMinor: Boolean
            get() = this == AgeStatus.SUPERVISED ||
                    this == AgeStatus.SUPERVISED_APPROVAL_PENDING ||
                    this == AgeStatus.SUPERVISED_APPROVAL_DENIED
    }
}
