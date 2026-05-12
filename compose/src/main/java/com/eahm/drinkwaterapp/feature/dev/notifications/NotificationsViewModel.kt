package com.eahm.drinkwaterapp.feature.dev.notifications

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager.Companion.DRINK_ALARM_ID
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.data.systemnotifications.NotificationService
import com.eahm.drinkwaterapp.system.alarm.AlarmScheduler
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class NotificationsViewModel(
    private val application: Application,
    notificationPermissionObserver: NotificationPermissionObserver,
) : ViewModel() {

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    @OptIn(ExperimentalTime::class)
    private val time = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            TimeSelected(it.hour, it.minute)
        }
    )

    val state = combine(
        notificationPermissionObserver.hasPermission,
        time,
    ) { hasNotificationPermission, time ->
        NotificationsState.Data(
            hasNotificationPermissions = hasNotificationPermission,
            extraTime = time,
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
            is NotificationsAction.OnExtraTimeChanged -> time.update { action.time }
        }
    }

    private fun showNotificationUsingBroadcast() {
        val intent = AlarmScheduler.getAlarmIntent(
            context = application,
            alarmId = DRINK_ALARM_ID,
            millisToTriggerFromNow = 0,
            extraTriggerHour = time.value.hour,
            extraTriggerMinute = time.value.minute,
        )

        application.sendBroadcast(intent)
    }

    private fun showNotification() {
        viewModelScope.launch {
            NotificationService.showDrinkNotification(
                context = application,
                hour = time.value.hour,
                minute = time.value.minute,
            )
        }
    }
}
