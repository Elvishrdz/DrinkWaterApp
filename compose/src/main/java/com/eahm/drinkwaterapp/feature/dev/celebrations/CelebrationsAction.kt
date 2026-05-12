package com.eahm.drinkwaterapp.feature.dev.celebrations

internal sealed interface CelebrationsAction {

    data object ShowDayCelebration : CelebrationsAction
}
