package com.eahm.drinkwaterapp.age.api

/**
 * Persistence for the developer-menu API override, so a pinned response survives an app restart and
 * is applied on the next startup. Implemented by the app's shared DataStore. Reads are honored only
 * on debug builds (see [AgeDebugController.isAvailable]).
 */
interface AgeDebugPreferences {
    suspend fun isOverrideEnabled(): Boolean
    suspend fun setOverrideEnabled(enabled: Boolean)

    /** Selected response enum name, or null if none is pinned. */
    suspend fun getSelectedResponse(): String?
    suspend fun setSelectedResponse(name: String?)
}
