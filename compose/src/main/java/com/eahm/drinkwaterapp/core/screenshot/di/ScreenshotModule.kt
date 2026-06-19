package com.eahm.drinkwaterapp.core.screenshot.di

import com.eahm.drinkwaterapp.core.config.BuildEnvironment
import com.eahm.drinkwaterapp.core.screenshot.DefaultScreenshotModeController
import com.eahm.drinkwaterapp.core.screenshot.NoOpScreenshotModeController
import com.eahm.drinkwaterapp.core.screenshot.ScreenshotModeController
import org.koin.dsl.module

val screenshotModule = module {
    single<ScreenshotModeController> {
        // Release ships a no-op so screenshot mode is unavailable to end users; other builds get
        // the persisted, DataStore-backed controller.
        if (BuildEnvironment.isProduction) {
            NoOpScreenshotModeController()
        } else {
            DefaultScreenshotModeController(dataStore = get())
        }
    }
}
