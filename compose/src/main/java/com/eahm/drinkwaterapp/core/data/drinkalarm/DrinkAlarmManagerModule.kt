package com.eahm.drinkwaterapp.core.data.drinkalarm

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val drinkAlarmModule = module {
    singleOf(::DrinkAlarmManager)
}
