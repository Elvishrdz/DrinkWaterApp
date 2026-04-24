package com.eahm.drinkwaterapp.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val prefsStorageModule = module {
    singleOf(::DataStorePrefsStorage) bind PrefsStorage::class
}

interface PrefsStorage {

    fun getFirstSetupDone(): Flow<Boolean>
    suspend fun setFirstSetupDone(value: Boolean)

    fun getGoalAmount(): Flow<Float>
    suspend fun setGoalAmount(amount: Float)

    suspend fun clear()
}

class DataStorePrefsStorage(
    private val dataStore: DataStore<Preferences>,
) : PrefsStorage {

    private val firstSetupKey = booleanPreferencesKey("FIRST_SETUP")
    private val goalAmountKey = floatPreferencesKey("GOAL_AMOUNT")

    override fun getFirstSetupDone(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[firstSetupKey] ?: false
        }
    }

    override suspend fun setFirstSetupDone(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[firstSetupKey] = value
                }
        }
    }

    override fun getGoalAmount(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[goalAmountKey] ?: 1000f
        }
    }

    override suspend fun setGoalAmount(amount: Float) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[goalAmountKey] = amount
                }
        }
    }


    override suspend fun clear() {
        dataStore.edit {
            it.remove(firstSetupKey)
            it.remove(goalAmountKey)
        }
    }
}
