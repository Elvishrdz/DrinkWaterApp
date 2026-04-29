package com.eahm.drinkwaterapp.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

    fun getUsername(): Flow<String?>
    suspend fun setUsername(name: String)

    fun getDrinkPresets(): Flow<Set<DrinkPreset>>
    suspend fun setDrinkPresets(presets: Set<DrinkPreset>)


    suspend fun clear()
}

@Serializable
data class DrinkPreset(
    val order: Int,
    val amountMl: Float,
)

private class DataStorePrefsStorage(
    private val dataStore: DataStore<Preferences>,
) : PrefsStorage {

    private val drinkDefaultPresets = setOf(
        DrinkPreset(1, 100f),
        DrinkPreset(2, 250f),
        DrinkPreset(3, 500f),
    )

    private val firstSetupKey = booleanPreferencesKey("FIRST_SETUP")
    private val goalAmountKey = floatPreferencesKey("GOAL_AMOUNT")
    private val usernameKey = stringPreferencesKey("USERNAME")
    private val drinkPresetsKey = stringPreferencesKey("DRINKS_PRESETS")

    private val json = Json {
        ignoreUnknownKeys = true
    }

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

    override fun getUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[usernameKey]
        }
    }

    override suspend fun setUsername(name: String) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[usernameKey] = name
                }
        }
    }

    override fun getDrinkPresets(): Flow<Set<DrinkPreset>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[drinkPresetsKey]
            jsonString?.let {
                runCatching {
                    json.decodeFromString<Set<DrinkPreset>>(it)
                }.getOrElse {
                    null
                }
            } ?: drinkDefaultPresets
        }
    }

    override suspend fun setDrinkPresets(presets: Set<DrinkPreset>) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    val jsonString = Json.encodeToString(presets)
                    it[drinkPresetsKey] = jsonString
                }
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(firstSetupKey)
            it.remove(goalAmountKey)
            it.remove(usernameKey)
            it.remove(drinkPresetsKey)
        }
    }
}
