package com.eahm.drinkwaterapp.core.data.datastore

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.coroutines.flow.Flow

interface PrefsStorage {

    fun getPrivacyPolicyAcceptedAt(): Flow<Long?>
    suspend fun setPrivacyPolicyAccepted(acceptedAt: Long)
    suspend fun clearPrivacyPolicyAccepted()

    fun getPrivacyPolicyVersionAccepted(): Flow<Long?>
    suspend fun setPrivacyPolicyVersionAccepted(policyDate: Long)
    suspend fun clearPrivacyPolicyVersionAccepted()

    fun getTrackingConsentDecided(): Flow<Boolean>
    suspend fun setTrackingConsentDecided()
    suspend fun clearTrackingConsentDecided()

    fun getOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(value: Boolean)

    fun getUsername(): Flow<String?>
    suspend fun setUsername(name: String)

    fun getDrinkSuggestions(): Flow<Set<DrinkSuggestion>>
    suspend fun setDrinkSuggestions(suggestions: Set<DrinkSuggestion>)

    fun getInterval(): Flow<ActiveTime>
    suspend fun setInterval(interval: ActiveTime)

    fun getStartTime(): Flow<ActiveTime>
    suspend fun setStartTime(time: ActiveTime)

    fun getEndTime(): Flow<ActiveTime>
    suspend fun setEndTime(time: ActiveTime)

    fun getAmountPerDrink(): Flow<Float>
    suspend fun setAmountPerDrink(milliliters: Float)

    fun getBestGoalStreak(): Flow<Int>
    suspend fun setBestGoalStreak(days: Int)

    fun getBestDrinkStreak(): Flow<Int>
    suspend fun setBestDrinkStreak(days: Int)

    fun getTimeFormatPreference(): Flow<TimeFormatPreference>
    suspend fun setTimeFormatPreference(pref: TimeFormatPreference)

    fun getUnitPreference(): Flow<UnitPreference>
    suspend fun setUnitPreference(pref: UnitPreference)

    fun getFirstDrinkDialogSeen(): Flow<Boolean>
    suspend fun setFirstDrinkDialogSeen()

    fun getWaterAnimationEnabled(): Flow<Boolean>
    suspend fun setWaterAnimationEnabled(enabled: Boolean)

    /** Returns the set of active days-of-week as ISO values (1=Mon … 7=Sun). Defaults to all 7. */
    fun getActiveDays(): Flow<Set<Int>>
    suspend fun setActiveDays(days: Set<Int>)

    // --- Cloud Backup ---
    suspend fun getBackupEnabled(): Boolean
    suspend fun setBackupEnabled(enabled: Boolean)

    suspend fun getBackupProvider(): String?
    suspend fun setBackupProvider(id: String)

    suspend fun getBackupLastSyncAt(): Long?
    suspend fun setBackupLastSyncAt(millis: Long)

    suspend fun getBackupAutoIntervalDays(): Int
    suspend fun setBackupAutoIntervalDays(days: Int)

    suspend fun getBackupUserDisplayName(): String?
    suspend fun setBackupUserDisplayName(name: String?)

    suspend fun getBackupContentHash(): String?
    suspend fun setBackupContentHash(hash: String?)

    suspend fun getBackupLastError(): String?
    suspend fun setBackupLastError(code: String?)

    suspend fun clear()
}