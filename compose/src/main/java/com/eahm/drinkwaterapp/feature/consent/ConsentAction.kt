package com.eahm.drinkwaterapp.feature.consent

internal sealed interface ConsentAction {

    data class SetPrivacyToggle(val checked: Boolean) : ConsentAction
    data object AcceptPrivacyPolicy : ConsentAction
    data object AllowTracking : ConsentAction
    data object DeclineTracking : ConsentAction
}
