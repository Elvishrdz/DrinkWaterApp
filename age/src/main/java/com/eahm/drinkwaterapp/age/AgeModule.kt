package com.eahm.drinkwaterapp.age

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
 * Note: [com.eahm.drinkwaterapp.age.api.AgePreferences] is **not** provided here — it is implemented
 * by the app's shared DataStore (mirroring `:backup`'s `BackupPreferences`) and must be registered
 * by the app before this module is loaded.
 */
val ageModule = module {
    // The switchable provider wraps the real Play provider; in production its override is null.
    single { SwitchableAgeSignalsProvider(PlayAgeSignalsProvider(androidContext())) }
    single<AgeSignalsProvider> { get<SwitchableAgeSignalsProvider>() }
    single<AgeDebugController> { get<SwitchableAgeSignalsProvider>() }
    single<AgeRepository> { AgeRepositoryImpl(provider = get(), preferences = get()) }
}
