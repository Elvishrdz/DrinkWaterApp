package com.eahm.drinkwaterapp.core.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

/**
 * Reads the current [ScreenshotModeState] in composition. Convenience wrapper so feature UIs can
 * gate on screenshot mode without each wiring up the controller injection + flow collection.
 */
@Composable
fun screenshotModeState(): ScreenshotModeState {
    val controller = koinInject<ScreenshotModeController>()
    val state by controller.state.collectAsStateWithLifecycle()
    return state
}
