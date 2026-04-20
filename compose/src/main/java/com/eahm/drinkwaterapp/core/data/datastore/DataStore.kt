package com.eahm.drinkwaterapp.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private const val DataStoreFileName = "drink.water.app.preferences_pb"

val dataStoreModule: Module = module {
    single<DataStore<Preferences>> { createDataStore(context = androidContext()) }
}

fun createDataStore(
    context: Context,
): DataStore<Preferences> = createDataStore {
    context.filesDir.resolve(DataStoreFileName).absolutePath
}

fun createDataStore(
    producePath: () -> String,
): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = { producePath().toPath() }
)
