package com.eahm.drinkwaterapp.age.api

/**
 * Our own representation of the Play Age Signals verification status, decoupled from the Play SDK's
 * `AgeSignalsVerificationStatus`. Keeping this Play-free lets the `api` package stay
 * multiplatform-ready for a future iOS `expect/actual` implementation.
 */
enum class AgeStatus {
    /** Age verified via government ID, credit card, or facial estimation. */
    VERIFIED,

    /** Age declared by the user, a parent, or a legal guardian. */
    DECLARED,

    /** Supervised Google Account with a parent-set age. */
    SUPERVISED,

    /** Supervised account with a pending approval for significant changes. */
    SUPERVISED_APPROVAL_PENDING,

    /** Parent denied approval for significant changes. */
    SUPERVISED_APPROVAL_DENIED,
}
