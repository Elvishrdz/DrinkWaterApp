package com.eahm.drinkwaterapp.feature.dev.alarmintervals

// UI state (Loading skeleton → Data)
internal sealed interface AlarmIntervalsState {
    data object Loading : AlarmIntervalsState
    class Data(
        // fields added as the feature grows
    ) : AlarmIntervalsState
}

// User intents (UI → ViewModel)
internal sealed interface AlarmIntervalsAction {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface AlarmIntervalsEvent {
    // events added as the feature grows
}
