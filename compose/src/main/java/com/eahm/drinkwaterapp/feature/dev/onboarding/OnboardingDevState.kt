package com.eahm.drinkwaterapp.feature.dev.onboarding

internal sealed interface OnboardingDevState {

    data object Loading : OnboardingDevState
    data class Data(val isCompleted: Boolean) : OnboardingDevState
}
