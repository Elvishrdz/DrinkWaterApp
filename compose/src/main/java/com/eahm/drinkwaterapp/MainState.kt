package com.eahm.drinkwaterapp

sealed interface MainState {

    data object Startup : MainState

    data class Ready(
        val onboardingCompleted: Boolean,
    ) : MainState
}
