package com.eahm.drinkwaterapp.core.data.datastore

import android.icu.util.LocaleData
import android.icu.util.ULocale
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.model.SeenDevice
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val prefsStorageModule = module {
    single<DataStorePrefsStorage> { DataStorePrefsStorage(get()) }
    single<PrefsStorage> { get<DataStorePrefsStorage>() }
    single<BackupPreferences> { get<DataStorePrefsStorage>() }
}

val initialInterval = ActiveTime(0, 45)
const val initialAmountPerDrink = 150f

internal class DataStorePrefsStorage(
    private val dataStore: DataStore<Preferences>,
) : PrefsStorage, BackupPreferences {

    private val initialDrinkSuggestions = setOf(
        DrinkSuggestion(1, 50f),
        DrinkSuggestion(2, 100f),
        DrinkSuggestion(3, 200f),

        DrinkSuggestion(4, 10f),
        DrinkSuggestion(5, 150f),
        DrinkSuggestion(6, 250f),
    )

    private val privacyPolicyAcceptedAtKey = longPreferencesKey("PRIVACY_POLICY_ACCEPTED_AT")
    private val privacyPolicyVersionAcceptedKey =
        longPreferencesKey("PRIVACY_POLICY_VERSION_ACCEPTED")
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
    private val firstDrinkDialogSeenKey = booleanPreferencesKey("FIRST_DRINK_DIALOG_SEEN")
    private val waterAnimationEnabledKey = booleanPreferencesKey("WATER_ANIMATION_ENABLED")
    private val activeDaysKey = stringPreferencesKey("ACTIVE_DAYS")

    // Backup keys
    private val backupEnabledKey = booleanPreferencesKey("BACKUP_ENABLED")

    private val backupProviderKey = stringPreferencesKey("BACKUP_PROVIDER")
    private val backupLastSyncAtKey = longPreferencesKey("BACKUP_LAST_SYNC_AT")
    private val backupAutoHourKey = intPreferencesKey("BACKUP_AUTO_HOUR")
    private val backupUserDisplayNameKey = stringPreferencesKey("BACKUP_USER_DISPLAY_NAME")
    private val backupContentHashKey = stringPreferencesKey("BACKUP_CONTENT_HASH")
    private val backupLastErrorKey = stringPreferencesKey("BACKUP_LAST_ERROR")
    private val backupLastFailureKey = stringPreferencesKey("BACKUP_LAST_FAILURE")
    private val devBackupNotificationModeKey =
        stringPreferencesKey("DEV_BACKUP_NOTIFICATION_MODE")

    // Sync / device identity keys
    private val deviceIdKey = stringPreferencesKey("SYNC_DEVICE_ID")
    private val syncPendingKey = booleanPreferencesKey("SYNC_PENDING")
    private val lastRemoteSyncAtKey = longPreferencesKey("SYNC_LAST_REMOTE_SYNC_AT")
    private val syncOnSaveEnabledKey = booleanPreferencesKey("SYNC_ON_SAVE_ENABLED")
    private val seenDevicesKey = stringPreferencesKey("SYNC_SEEN_DEVICES")
    private val tombstoneWarningPendingKey = booleanPreferencesKey("SYNC_TOMBSTONE_WARNING_PENDING")
    private val initialSyncPendingKey = booleanPreferencesKey("BACKUP_INITIAL_SYNC_PENDING")

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

    override fun getPrivacyPolicyVersionAccepted(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[privacyPolicyVersionAcceptedKey]
        }
    }

    override suspend fun setPrivacyPolicyVersionAccepted(policyDate: Long) {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences()
                .also { it[privacyPolicyVersionAcceptedKey] = policyDate }
        }
    }

    override suspend fun clearPrivacyPolicyVersionAccepted() {
        dataStore.edit { it.remove(privacyPolicyVersionAcceptedKey) }
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

    private fun localeDefaultUnitPreference(): UnitPreference {
        val system = LocaleData.getMeasurementSystem(ULocale.getDefault())
        return when (system) {
            LocaleData.MeasurementSystem.US -> UnitPreference.FL_OZ
            else -> UnitPreference.ML
        }
    }

    override suspend fun setUnitPreference(pref: UnitPreference) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[unitPreferenceKey] = pref.name }
        }
    }

    override fun getFirstDrinkDialogSeen(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[firstDrinkDialogSeenKey] ?: false }

    override suspend fun setFirstDrinkDialogSeen() {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[firstDrinkDialogSeenKey] = true }
        }
    }

    override fun getWaterAnimationEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[waterAnimationEnabledKey] ?: true }

    override suspend fun setWaterAnimationEnabled(enabled: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[waterAnimationEnabledKey] = enabled }
        }
    }

    @Suppress("MagicNumber")
    override fun getActiveDays(): Flow<Set<Int>> =
        dataStore.data.map { prefs ->
            prefs[activeDaysKey]
                ?.let { runCatching { json.decodeFromString<Set<Int>>(it) }.getOrNull() }
                ?: setOf(1, 2, 3, 4, 5, 6, 7)
        }

    override suspend fun setActiveDays(days: Set<Int>) {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().also { it[activeDaysKey] = Json.encodeToString(days) }
        }
    }

    // --- Backup prefs implementation ---

    override suspend fun getBackupEnabled(): Boolean =
        dataStore.data.map { it[backupEnabledKey] ?: false }.first()

    override suspend fun setBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[backupEnabledKey] = enabled }
    }

    override suspend fun getBackupProvider(): String? =
        dataStore.data.map { it[backupProviderKey] }.first()

    override suspend fun setBackupProvider(id: String) {
        dataStore.edit { it[backupProviderKey] = id }
    }

    override suspend fun getBackupLastSyncAt(): Long? =
        dataStore.data.map { it[backupLastSyncAtKey] }.first()

    override suspend fun setBackupLastSyncAt(millis: Long) {
        dataStore.edit { it[backupLastSyncAtKey] = millis }
    }

    override fun getBackupLastSyncAtFlow(): Flow<Long?> =
        dataStore.data.map { it[backupLastSyncAtKey] }

    override suspend fun getBackupAutoHour(): Int =
        dataStore.data.map { it[backupAutoHourKey] ?: DEFAULT_BACKUP_HOUR }.first()

    override suspend fun setBackupAutoHour(hour: Int) {
        dataStore.edit { it[backupAutoHourKey] = hour.coerceIn(MIN_HOUR, MAX_HOUR) }
    }

    override suspend fun getBackupUserDisplayName(): String? =
        dataStore.data.map { it[backupUserDisplayNameKey] }.first()

    override suspend fun setBackupUserDisplayName(name: String?) {
        dataStore.edit { prefs ->
            if (name != null) prefs[backupUserDisplayNameKey] = name
            else prefs.remove(backupUserDisplayNameKey)
        }
    }

    override suspend fun getBackupContentHash(): String? =
        dataStore.data.map { it[backupContentHashKey] }.first()

    override suspend fun setBackupContentHash(hash: String?) {
        dataStore.edit { prefs ->
            if (hash != null) prefs[backupContentHashKey] = hash
            else prefs.remove(backupContentHashKey)
        }
    }

    override suspend fun getBackupLastError(): String? =
        dataStore.data.map { it[backupLastErrorKey] }.first()

    override suspend fun setBackupLastError(code: String?) {
        dataStore.edit { prefs ->
            if (code != null) prefs[backupLastErrorKey] = code
            else prefs.remove(backupLastErrorKey)
        }
    }

    override suspend fun getDevBackupNotificationModeName(): String? =
        dataStore.data.map { it[devBackupNotificationModeKey] }.first()

    override suspend fun setDevBackupNotificationModeName(name: String) {
        dataStore.edit { it[devBackupNotificationModeKey] = name }
    }

    override suspend fun getBackupLastFailure(): String? =
        dataStore.data.map { it[backupLastFailureKey] }.first()

    override suspend fun setBackupLastFailure(value: String?) {
        dataStore.edit { prefs ->
            if (value != null) {
                prefs[backupLastFailureKey] = value
            } else {
                prefs.remove(backupLastFailureKey)
            }
        }
    }

    override suspend fun clearBackupKeys() {
        dataStore.edit { prefs ->
            // NOTE: deviceIdKey is intentionally NOT cleared — the device identifier is a
            // stable, app-scoped UUID that must persist across disconnect/reconnect and
            // account changes (see multiplatform-sync plan §2).
            prefs.remove(backupEnabledKey)
            prefs.remove(backupProviderKey)
            prefs.remove(backupLastSyncAtKey)
            prefs.remove(backupAutoHourKey)
            prefs.remove(backupUserDisplayNameKey)
            prefs.remove(backupContentHashKey)
            prefs.remove(backupLastErrorKey)
            prefs.remove(backupLastFailureKey)
            prefs.remove(syncPendingKey)
            prefs.remove(lastRemoteSyncAtKey)
            prefs.remove(syncOnSaveEnabledKey)
            prefs.remove(seenDevicesKey)
            prefs.remove(tombstoneWarningPendingKey)
            prefs.remove(initialSyncPendingKey)
        }
    }

    // --- Sync / device identity prefs implementation ---

    /**
     * Returns the stable, app-scoped device identifier, generating and persisting a UUID
     * on first access. Generated once, never changed — survives account changes and
     * disconnect (see multiplatform-sync plan §2). NOT the Android ID.
     */
    override suspend fun getDeviceId(): String {
        val existing = dataStore.data.map { it[deviceIdKey] }.first()
        if (!existing.isNullOrBlank()) return existing

        // Generate atomically: re-check inside the transaction so concurrent callers
        // can't each create a different id.
        var result = ""
        dataStore.edit { prefs ->
            val current = prefs[deviceIdKey]
            result = if (!current.isNullOrBlank()) {
                current
            } else {
                java.util.UUID.randomUUID().toString().also { prefs[deviceIdKey] = it }
            }
        }
        return result
    }

    override suspend fun setDeviceId(id: String) {
        dataStore.edit { it[deviceIdKey] = id }
    }

    override suspend fun getSyncPending(): Boolean =
        dataStore.data.map { it[syncPendingKey] ?: false }.first()

    override suspend fun setSyncPending(pending: Boolean) {
        dataStore.edit { it[syncPendingKey] = pending }
    }

    override suspend fun getLastRemoteSyncAt(): Long? =
        dataStore.data.map { it[lastRemoteSyncAtKey] }.first()

    override suspend fun setLastRemoteSyncAt(millis: Long) {
        dataStore.edit { it[lastRemoteSyncAtKey] = millis }
    }

    override suspend fun getSyncOnSaveEnabled(): Boolean =
        dataStore.data.map { it[syncOnSaveEnabledKey] ?: true }.first()

    override suspend fun setSyncOnSaveEnabled(enabled: Boolean) {
        dataStore.edit { it[syncOnSaveEnabledKey] = enabled }
    }

    override suspend fun getSeenDevices(): List<SeenDevice> =
        dataStore.data.map { prefs ->
            prefs[seenDevicesKey]?.let { raw ->
                runCatching { json.decodeFromString<List<SeenDevice>>(raw) }.getOrNull()
            } ?: emptyList()
        }.first()

    override suspend fun setSeenDevices(devices: List<SeenDevice>) {
        dataStore.edit { it[seenDevicesKey] = json.encodeToString(devices) }
    }

    override suspend fun getTombstoneWarningPending(): Boolean =
        dataStore.data.map { it[tombstoneWarningPendingKey] ?: false }.first()

    override suspend fun setTombstoneWarningPending(pending: Boolean) {
        dataStore.edit { it[tombstoneWarningPendingKey] = pending }
    }

    override suspend fun getInitialSyncPending(): Boolean =
        dataStore.data.map { it[initialSyncPendingKey] ?: false }.first()

    override suspend fun setInitialSyncPending(pending: Boolean) {
        dataStore.edit { it[initialSyncPendingKey] = pending }
    }

    override suspend fun clear() {
        clearPrivacyPolicyAccepted()
        clearPrivacyPolicyVersionAccepted()
        clearTrackingConsentDecided()
        clearBackupKeys()
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

    private companion object {
        const val DEFAULT_BACKUP_HOUR = 2
        const val MIN_HOUR = 0
        const val MAX_HOUR = 23
    }
}
