package com.eahm.drinkwaterapp.feature.dev.celebrations

internal sealed interface CelebrationsState {

    data object Loading : CelebrationsState

    class Data(
        // fields added as the feature grows
    ) : CelebrationsState
}
