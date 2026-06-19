package com.eahm.drinkwaterapp.core.screenshot

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * DataStore-backed [ScreenshotModeController] bound on non-release builds. State persists across app
 * restarts so screenshots that require relaunching keep the mode. Release builds use
 * [NoOpScreenshotModeController] instead, so this is never instantiated in production.
 */
internal class DefaultScreenshotModeController(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ScreenshotModeController {

    private val enabledKey = booleanPreferencesKey("SCREENSHOT_MODE_ENABLED")
    private val versionKey = stringPreferencesKey("SCREENSHOT_MODE_VERSION")

    override val state: StateFlow<ScreenshotModeState> = dataStore.data
        .map { prefs ->
            ScreenshotModeState(
                enabled = prefs[enabledKey] ?: false,
                displayVersion = prefs[versionKey].orEmpty(),
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, ScreenshotModeState())

    override suspend fun enable(version: String) {
        dataStore.edit {
            it[enabledKey] = true
            it[versionKey] = version.trim()
        }
    }

    override suspend fun disable() {
        dataStore.edit { it[enabledKey] = false }
    }
}
