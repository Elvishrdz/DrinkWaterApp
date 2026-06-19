package com.eahm.drinkwaterapp.feature.dev.screenshots

sealed interface ScreenshotsDevState {
    data object Loading : ScreenshotsDevState

    /**
     * @param version footer version string applied when screenshot mode is enabled (no suffix).
     * @param screenshotModeEnabled whether the app is currently presenting in screenshot mode.
     * @param isRunning whether an insert/clear job is in progress.
     * @param resultMessage human-readable outcome of the last action.
     */
    data class Data(
        val version: String,
        val screenshotModeEnabled: Boolean,
        val isRunning: Boolean = false,
        val resultMessage: String = "",
    ) : ScreenshotsDevState
}
