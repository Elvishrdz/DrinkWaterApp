package com.eahm.drinkwaterapp.age

import android.content.pm.ApplicationInfo
import com.eahm.drinkwaterapp.age.api.AgeDebugController
import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.eahm.drinkwaterapp.age.internal.AgeRepositoryImpl
import com.eahm.drinkwaterapp.age.internal.PlayAgeSignalsProvider
import com.eahm.drinkwaterapp.age.internal.SwitchableAgeSignalsProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for the `:age` feature.
 *
 * Note: [com.eahm.drinkwaterapp.age.api.AgePreferences] and
 * [com.eahm.drinkwaterapp.age.api.AgeDebugPreferences] are **not** provided here — they are
 * implemented by the app's shared DataStore (mirroring `:backup`'s `BackupPreferences`) and must be
 * registered by the app before this module is loaded.
 */
val ageModule = module {
    single {
        // Override is honored only on debuggable builds; release builds always use the real API.
        val isDebuggable =
            (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        SwitchableAgeSignalsProvider(
            delegate = PlayAgeSignalsProvider(androidContext()),
            debugPreferences = get(),
            isAvailable = isDebuggable,
        )
    }
    single<AgeSignalsProvider> { get<SwitchableAgeSignalsProvider>() }
    single<AgeDebugController> { get<SwitchableAgeSignalsProvider>() }
    single<AgeRepository> { AgeRepositoryImpl(provider = get(), preferences = get()) }
}
