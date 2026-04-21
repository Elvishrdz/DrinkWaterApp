package com.eahm.drinkwaterapp.core.data.drinkalarm

import org.koin.dsl.module

val drinkAlarmModule = module {
    single<DrinkAlarmManager> { DrinkAlarmManager(get()) }
}
