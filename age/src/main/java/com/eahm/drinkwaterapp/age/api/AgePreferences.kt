package com.eahm.drinkwaterapp.age.api

/**
 * Persistence contract for the resolved/declared age signal. Implemented by the app's shared
 * DataStore (mirroring how `:backup` declares `BackupPreferences`), so `:age` does not own its own
 * storage.
 */
interface AgePreferences {

    /** The last persisted signal (declared or API-sourced), or null if none has ever been stored. */
    suspend fun getAgeSignals(): AgeSignals?

    /** Persists the resolved/declared signal. */
    suspend fun setAgeSignals(signals: AgeSignals)

    /** Clears any stored age data. */
    suspend fun clearAgeSignals()
}
