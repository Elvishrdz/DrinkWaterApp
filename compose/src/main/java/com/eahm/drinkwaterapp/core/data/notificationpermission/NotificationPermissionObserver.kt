package com.eahm.drinkwaterapp.core.data.notificationpermission

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationPermissionObserver(
    private val application: Application,
) : DefaultLifecycleObserver, AppInitializer {

    private val _hasPermission = MutableStateFlow(application.canShowNotifications)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    override fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        _hasPermission.update { application.canShowNotifications }
    }
}
