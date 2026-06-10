package com.eahm.drinkwaterapp.age.api

import kotlinx.coroutines.flow.StateFlow

/**
 * App-facing entry point for age signals. Consumers (ads config, the bootstrap age gate, Settings)
 * depend only on this — never on the Play SDK.
 */
interface AgeRepository {

    /**
     * Best-known age signal. `null` means "no age constraint known" → callers should treat it as
     * [AdTreatment.STANDARD] (standard ads). Updated on [refresh] and [submitDeclaredBirthYear].
     */
    val ageSignals: StateFlow<AgeSignals?>

    /**
     * True only when the one-time bootstrap age screen should be shown: first run, no stored age,
     * and the API gave no usable signal ([AgeResolution.CoveredAgeUnknown] or
     * [AgeResolution.Undetermined]). Becomes false once any age is stored or the API returns a
     * definitive result.
     */
    val needsAgeCollection: StateFlow<Boolean>

    /**
     * Resolves the age signal from the API (call once per cold start) and reconciles it with any
     * stored value, updating [ageSignals] and [needsAgeCollection].
     */
    suspend fun refresh()

    /** Persists a user-declared age derived from the bootstrap screen's birth-year input. */
    suspend fun submitDeclaredBirthYear(birthYear: Int)

    /** Persists a user-declared age bracket chosen from the predefined options. */
    suspend fun submitDeclaredBracket(adTreatment: AdTreatment)
}
