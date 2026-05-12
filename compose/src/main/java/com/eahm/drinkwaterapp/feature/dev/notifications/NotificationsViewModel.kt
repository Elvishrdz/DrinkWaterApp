package com.eahm.drinkwaterapp.feature.dev.notifications

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.system.broadcast.AlarmReceiver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class NotificationsViewModel(
    private val application: Application,
    notificationPermissionObserver: NotificationPermissionObserver,
) : ViewModel() {

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    val state = notificationPermissionObserver.hasPermission.map {
        NotificationsState.Data(
            hasNotificationPermissions = it,
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = NotificationsState.Loading,
    )

    fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.ShowNotification -> showNotification()
            NotificationsAction.ShowNotificationUsingBroadcast -> showNotificationUsingBroadcast()
        }
    }

    private fun showNotificationUsingBroadcast() {
        val intent = Intent(AlarmReceiver.ALARM_ACTION).apply {
            setPackage(application.packageName)
        }
        application.sendBroadcast(intent)
    }

    private fun showNotification() {
        viewModelScope.launch {
            NotificationService.showDrinkNotification(application)
        }
    }
}
