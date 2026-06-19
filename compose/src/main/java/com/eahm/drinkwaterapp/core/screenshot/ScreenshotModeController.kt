package com.eahm.drinkwaterapp.core.screenshot

import kotlinx.coroutines.flow.StateFlow

/** Snapshot of the Play-Store-screenshot presentation mode. */
data class ScreenshotModeState(
    val enabled: Boolean = false,
    /** Version string shown in the settings footer while enabled (no build-variant suffix). */
    val displayVersion: String = "",
)

/**
 * Controls "screenshot mode": a debug-only presentation state that makes the app look like the
 * release build — ads off, dev-only menus hidden (REMINDERS, DEV), Dropbox hidden, and a
 * release-style version footer — so the team can capture Play Store screenshots.
 *
 * The mode is **inert on release builds**: [enable] is a no-op and [state] always reports
 * disabled there, so it can never affect production users. It is disabled at runtime only via the
 * 10-tap gesture on the home progress ring (the dev menu that enables it is itself hidden while on).
 */
interface ScreenshotModeController {
    val state: StateFlow<ScreenshotModeState>
    suspend fun enable(version: String)
    suspend fun disable()
}
