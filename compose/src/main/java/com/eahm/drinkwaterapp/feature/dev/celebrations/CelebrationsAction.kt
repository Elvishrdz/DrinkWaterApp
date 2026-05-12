package com.eahm.drinkwaterapp.feature.dev.celebrations

internal sealed interface CelebrationsAction {

    data object ShowOceanStyle : CelebrationsAction
    data object ShowCarnivalStyle : CelebrationsAction
}
