package com.eahm.drinkwaterapp.feature.dev.alarmsv2

internal sealed interface AlarmsV2Event {

    data object OnResumeCalled : AlarmsV2Event
}
