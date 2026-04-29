package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.feature.dev.DevViewModel
import com.eahm.drinkwaterapp.feature.history.HistoryViewModel
import com.eahm.drinkwaterapp.feature.settings.SettingsViewModel
import com.eahm.drinkwaterapp.feature.stats.StatsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDrinkModule = module {
    viewModelOf(::DrinkViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::DevViewModel)
}
