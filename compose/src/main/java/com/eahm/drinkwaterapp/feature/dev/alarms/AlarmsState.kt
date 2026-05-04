package com.eahm.drinkwaterapp.feature.dev.alarms

// UI state (Loading skeleton → Data)
internal sealed interface AlarmsState {
    data object Loading : AlarmsState
    class Data(
        // fields added as the feature grows
    ) : AlarmsState
}

// User intents (UI → ViewModel)
internal sealed interface AlarmsAction {
    // actions added as the feature grows
}

// One-shot side effects (ViewModel → UI)
internal sealed interface AlarmsEvent {
    // events added as the feature grows
}
