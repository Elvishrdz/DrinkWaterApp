package com.eahm.drinkwaterapp.core.data.datastore

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import kotlinx.coroutines.flow.Flow

interface PrefsStorage {

    fun getFirstSetupDone(): Flow<Boolean>
    suspend fun setFirstSetupDone(value: Boolean)

    fun getGoalAmount(): Flow<Float>
    suspend fun setGoalAmount(amount: Float)

    fun getUsername(): Flow<String?>
    suspend fun setUsername(name: String)

    fun getDrinkSuggestions(): Flow<Set<DrinkSuggestion>>
    suspend fun setDrinkSuggestions(suggestions: Set<DrinkSuggestion>)

    fun getStartTime(): Flow<ActiveTime>
    suspend fun setStartTime(time: ActiveTime)

    fun getEndTime(): Flow<ActiveTime>
    suspend fun setEndTime(time: ActiveTime)

    fun getAmountPerDrink(): Flow<Float>
    suspend fun setAmountPerDrink(milliliters: Float)

    suspend fun clear()
}