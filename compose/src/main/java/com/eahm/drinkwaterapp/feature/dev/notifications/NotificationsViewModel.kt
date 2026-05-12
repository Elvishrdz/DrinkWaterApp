package com.eahm.drinkwaterapp.feature.dev.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class NotificationsViewModel : ViewModel() {

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state: StateFlow<NotificationsState> = _state

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    fun onAction(action: NotificationsAction) {
        // handled as the feature grows
    }
}
