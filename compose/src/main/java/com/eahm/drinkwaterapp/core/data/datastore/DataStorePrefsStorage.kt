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

    fun getStartTime(): Flow<ActiveTime>
    suspend fun setStartTime(time: ActiveTime)

    fun getEndTime(): Flow<ActiveTime>
    suspend fun setEndTime(time: ActiveTime)

    fun getAmountPerDrink(): Flow<Float>
    suspend fun setAmountPerDrink(milliliters: Float)

    fun getNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)

    suspend fun clear()
}

@Serializable
data class ActiveTime(
    val hour: Int,
    val minute: Int,
)

@Serializable
data class DrinkPreset(
    val order: Int,
    val amountMl: Float,
)

private class DataStorePrefsStorage(
    private val dataStore: DataStore<Preferences>,
) : PrefsStorage {

    private val drinkDefaultPresets = setOf(
        DrinkPreset(1, 50f),
        DrinkPreset(2, 100f),
        DrinkPreset(3, 200f),

        DrinkPreset(4, 10f),
        DrinkPreset(5, 150f),
        DrinkPreset(6, 250f),
    )

    private val firstSetupKey = booleanPreferencesKey("FIRST_SETUP")
    private val goalAmountKey = floatPreferencesKey("GOAL_AMOUNT")
    private val usernameKey = stringPreferencesKey("USERNAME")
    private val drinkPresetsKey = stringPreferencesKey("DRINKS_PRESETS")
    private val startTimeKey = stringPreferencesKey("START_TIME")
    private val endTimeKey = stringPreferencesKey("END_TIME")
    private val amountPerDrinkKey = floatPreferencesKey("AMOUNT_PER_DRINK")
    private val notificationEnabledKey = booleanPreferencesKey("NOTIFICATION_ENABLED")

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
                runCatching { json.decodeFromString<Set<DrinkPreset>>(it) }
                    .getOrNull()
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

    override fun getStartTime(): Flow<ActiveTime> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[startTimeKey]
            jsonString?.let {
                runCatching { json.decodeFromString<ActiveTime>(it) }
                    .getOrNull()
            } ?: ActiveTime(10, 0)
        }
    }

    override suspend fun setStartTime(time: ActiveTime) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    val jsonString = Json.encodeToString(time)
                    it[startTimeKey] = jsonString
                }
        }
    }

    override fun getEndTime(): Flow<ActiveTime> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[endTimeKey]
            jsonString?.let {
                runCatching { json.decodeFromString<ActiveTime>(it) }
                    .getOrNull()
            } ?: ActiveTime(16, 0)
        }
    }

    override suspend fun setEndTime(time: ActiveTime) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    val jsonString = Json.encodeToString(time)
                    it[endTimeKey] = jsonString
                }
        }
    }

    override fun getAmountPerDrink(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[amountPerDrinkKey] ?: 150f
        }
    }

    override suspend fun setAmountPerDrink(milliliters: Float) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[amountPerDrinkKey] = milliliters
                }
        }
    }

    override fun getNotificationEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[notificationEnabledKey] ?: false
        }
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[notificationEnabledKey] = enabled
                }
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(firstSetupKey)
            it.remove(goalAmountKey)
            it.remove(usernameKey)
            it.remove(drinkPresetsKey)
            it.remove(startTimeKey)
            it.remove(endTimeKey)
            it.remove(amountPerDrinkKey)
            it.remove(notificationEnabledKey)
        }
    }
}
