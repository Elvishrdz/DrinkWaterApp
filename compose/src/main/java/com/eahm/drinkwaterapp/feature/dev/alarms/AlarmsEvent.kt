package com.eahm.drinkwaterapp.feature.dev.alarms

internal sealed interface AlarmsEvent {

    data object OnResumeCalled : AlarmsEvent
}
