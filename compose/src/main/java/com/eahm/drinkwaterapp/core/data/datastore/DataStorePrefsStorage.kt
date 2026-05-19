package com.eahm.drinkwaterapp.core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val prefsStorageModule = module {
    singleOf(::DataStorePrefsStorage) bind PrefsStorage::class
}

val initialInterval = ActiveTime(0, 45)
const val initialAmountPerDrink = 150f

private class DataStorePrefsStorage(
    private val dataStore: DataStore<Preferences>,
) : PrefsStorage {

    private val initialDrinkSuggestions = setOf(
        DrinkSuggestion(1, 50f),
        DrinkSuggestion(2, 100f),
        DrinkSuggestion(3, 200f),

        DrinkSuggestion(4, 10f),
        DrinkSuggestion(5, 150f),
        DrinkSuggestion(6, 250f),
    )

    private val privacyPolicyAcceptedAtKey = longPreferencesKey("PRIVACY_POLICY_ACCEPTED_AT")
    private val trackingConsentDecidedKey = booleanPreferencesKey("TRACKING_CONSENT_DECIDED")
    private val onboardingCompletedKey = booleanPreferencesKey("ONBOARDING_COMPLETED")
    private val usernameKey = stringPreferencesKey("USERNAME")
    private val drinkSuggestionsKey = stringPreferencesKey("DRINKS_SUGGESTIONS")
    private val intervalKey = stringPreferencesKey("DRINKS_SUGGESTIONS")
    private val startTimeKey = stringPreferencesKey("START_TIME")
    private val endTimeKey = stringPreferencesKey("END_TIME")
    private val amountPerDrinkKey = floatPreferencesKey("AMOUNT_PER_DRINK")
    private val bestGoalStreakKey = intPreferencesKey("BEST_GOAL_STREAK")
    private val bestDrinkStreakKey = intPreferencesKey("BEST_DRINK_STREAK")
    private val timeFormatPreferenceKey = stringPreferencesKey("TIME_FORMAT_PREFERENCE")
    private val unitPreferenceKey = stringPreferencesKey("UNIT_PREFERENCE")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun getPrivacyPolicyAcceptedAt(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[privacyPolicyAcceptedAtKey]
        }
    }

    override suspend fun setPrivacyPolicyAccepted(acceptedAt: Long) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also { it[privacyPolicyAcceptedAtKey] = acceptedAt }
        }
    }

    override suspend fun clearPrivacyPolicyAccepted() {
        dataStore.edit { it.remove(privacyPolicyAcceptedAtKey) }
    }

    override fun getTrackingConsentDecided(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[trackingConsentDecidedKey] ?: false
        }
    }

    override suspend fun setTrackingConsentDecided() {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also { it[trackingConsentDecidedKey] = true }
        }
    }

    override suspend fun clearTrackingConsentDecided() {
        dataStore.edit { it.remove(trackingConsentDecidedKey) }
    }

    override fun getOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[onboardingCompletedKey] ?: false
        }
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    it[onboardingCompletedKey] = value
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

    override fun getDrinkSuggestions(): Flow<Set<DrinkSuggestion>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[drinkSuggestionsKey]
            jsonString?.let {
                runCatching { json.decodeFromString<Set<DrinkSuggestion>>(it) }
                    .getOrNull()
            } ?: initialDrinkSuggestions
        }
    }

    override suspend fun setDrinkSuggestions(suggestions: Set<DrinkSuggestion>) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    val jsonString = Json.encodeToString(suggestions)
                    it[drinkSuggestionsKey] = jsonString
                }
        }
    }

    override fun getInterval(): Flow<ActiveTime> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[intervalKey]
            jsonString?.let {
                runCatching { json.decodeFromString<ActiveTime>(it) }
                    .getOrNull()
            } ?: initialInterval
        }
    }

    override suspend fun setInterval(interval: ActiveTime) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also {
                    val jsonString = Json.encodeToString(interval)
                    it[intervalKey] = jsonString
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
            preferences[amountPerDrinkKey] ?: initialAmountPerDrink
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

    override fun getBestGoalStreak(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[bestGoalStreakKey] ?: 0
        }
    }

    override suspend fun setBestGoalStreak(days: Int) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also { it[bestGoalStreakKey] = days }
        }
    }

    override fun getBestDrinkStreak(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[bestDrinkStreakKey] ?: 0
        }
    }

    override suspend fun setBestDrinkStreak(days: Int) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also { it[bestDrinkStreakKey] = days }
        }
    }

    override fun getTimeFormatPreference(): Flow<TimeFormatPreference> =
        dataStore.data.map { prefs ->
            prefs[timeFormatPreferenceKey]
                ?.let { runCatching { TimeFormatPreference.valueOf(it) }.getOrNull() }
                ?: TimeFormatPreference.SYSTEM
        }

    override suspend fun setTimeFormatPreference(pref: TimeFormatPreference) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[timeFormatPreferenceKey] = pref.name }
        }
    }

    override fun getUnitPreference(): Flow<UnitPreference> =
        dataStore.data.map { prefs ->
            prefs[unitPreferenceKey]
                ?.let { runCatching { UnitPreference.valueOf(it) }.getOrNull() }
                ?: localeDefaultUnitPreference()
        }

    private fun localeDefaultUnitPreference(): UnitPreference =
        if (Locale.getDefault().country.equals("US", ignoreCase = true)) UnitPreference.FL_OZ
        else UnitPreference.ML

    override suspend fun setUnitPreference(pref: UnitPreference) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[unitPreferenceKey] = pref.name }
        }
    }

    override suspend fun clear() {
        clearPrivacyPolicyAccepted()
        clearTrackingConsentDecided()
        dataStore.edit {
            it.remove(onboardingCompletedKey)
            it.remove(usernameKey)
            it.remove(drinkSuggestionsKey)
            it.remove(startTimeKey)
            it.remove(endTimeKey)
            it.remove(amountPerDrinkKey)
            it.remove(bestGoalStreakKey)
            it.remove(bestDrinkStreakKey)
            it.remove(timeFormatPreferenceKey)
            it.remove(unitPreferenceKey)
        }
    }
}
