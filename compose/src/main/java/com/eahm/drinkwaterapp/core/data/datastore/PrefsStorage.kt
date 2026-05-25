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

    suspend fun clear()
}