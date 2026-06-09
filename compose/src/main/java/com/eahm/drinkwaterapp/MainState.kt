package com.eahm.drinkwaterapp

sealed interface MainState {

    data object Startup : MainState

    data class Ready(
        val consentCompleted: Boolean,
        val onboardingCompleted: Boolean,
        val ageCollectionRequired: Boolean,
    ) : MainState
}
