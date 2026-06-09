package com.eahm.drinkwaterapp.age.api

/** Where a stored [AgeSignals] value came from. The API is authoritative and supersedes a declared value. */
enum class AgeSource {
    /** Resolved from the Play Age Signals API. */
    API,

    /** Collected from the user via the one-time bootstrap age screen. */
    DECLARED,
}
