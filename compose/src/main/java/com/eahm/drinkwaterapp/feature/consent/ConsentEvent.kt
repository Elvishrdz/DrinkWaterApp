package com.eahm.drinkwaterapp.feature.consent

internal sealed interface ConsentEvent {

    data object ConsentCompleted : ConsentEvent
}
