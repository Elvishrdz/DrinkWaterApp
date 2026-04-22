package com.eahm.drinkwaterapp.feature.drink

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDrinkModule = module {
    viewModelOf(::DrinkViewModel)
}
