package com.eahm.drinkwaterapp.core.screenshot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Release implementation of [ScreenshotModeController]: always disabled, [enable]/[disable] are
 * no-ops, and nothing is persisted. Bound on production builds so screenshot mode can never affect
 * end users and the DataStore-backed implementation is never instantiated.
 */
internal class NoOpScreenshotModeController : ScreenshotModeController {
    override val state: StateFlow<ScreenshotModeState> = MutableStateFlow(ScreenshotModeState())
    override suspend fun enable(version: String) = Unit
    override suspend fun disable() = Unit
}
