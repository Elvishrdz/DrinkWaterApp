package com.eahm.drinkwaterapp.age.api

/**
 * Low-level source of a single age resolution. The Android implementation wraps the Play Age
 * Signals API; a future iOS implementation could wrap Apple's Declared Age Range API behind the
 * same contract.
 */
fun interface AgeSignalsProvider {
    /** Performs one resolution attempt (including any internal retries for retryable errors). */
    suspend fun resolve(): AgeResolution
}
