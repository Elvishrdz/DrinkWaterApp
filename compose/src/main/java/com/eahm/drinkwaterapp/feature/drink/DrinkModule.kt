package com.eahm.drinkwaterapp.feature.drink

import com.eahm.drinkwaterapp.feature.history.HistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDrinkModule = module {
    viewModelOf(::DrinkViewModel)
    viewModelOf(::HistoryViewModel)
}
