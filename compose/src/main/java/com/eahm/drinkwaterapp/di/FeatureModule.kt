package com.eahm.drinkwaterapp.di

import com.eahm.drinkwaterapp.MainViewModel
import com.eahm.drinkwaterapp.feature.dev.DevViewModel
import com.eahm.drinkwaterapp.feature.dev.alarmcalculator.AlarmCalculatorViewModel
import com.eahm.drinkwaterapp.feature.dev.alarms.AlarmsViewModel
import com.eahm.drinkwaterapp.feature.drink.DrinkViewModel
import com.eahm.drinkwaterapp.feature.history.HistoryViewModel
import com.eahm.drinkwaterapp.feature.settings.SettingsViewModel
import com.eahm.drinkwaterapp.feature.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val featureModule = module {
    viewModelOf(::MainViewModel)

    viewModelOf(::DrinkViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)

    // development modules
    viewModelOf(::DevViewModel)
    viewModelOf(::AlarmCalculatorViewModel)
    viewModelOf(::AlarmsViewModel)
}
