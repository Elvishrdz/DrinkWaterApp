package com.eahm.drinkwaterapp.feature.dev.alarms

internal sealed interface AlarmsAction {

    data object OnResumeCalled : AlarmsAction
}
