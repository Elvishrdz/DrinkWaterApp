package com.eahm.drinkwaterapp.core.database.di

import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.getAndroidRoomDatabaseBuilder
import com.eahm.drinkwaterapp.core.database.getRoomDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        val builder = getAndroidRoomDatabaseBuilder(
            applicationContext = get(),
        )

        getRoomDatabase(builder)
    }
}
