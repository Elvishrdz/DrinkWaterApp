package com.eahm.drinkwaterapp.age

import com.eahm.drinkwaterapp.age.api.AgeRepository
import com.eahm.drinkwaterapp.age.api.AgeSignalsProvider
import com.eahm.drinkwaterapp.age.internal.AgeRepositoryImpl
import com.eahm.drinkwaterapp.age.internal.PlayAgeSignalsProvider
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
    single<AgeSignalsProvider> { PlayAgeSignalsProvider(androidContext()) }
    single<AgeRepository> { AgeRepositoryImpl(provider = get(), preferences = get()) }
}
