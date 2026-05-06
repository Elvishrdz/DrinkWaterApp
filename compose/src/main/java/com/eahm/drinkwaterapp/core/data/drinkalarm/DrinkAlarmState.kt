package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo

data class DrinkAlarmState(
    val active: Boolean,
    val interval: AlarmInterval,
    val nextInterval: AlarmInterval?,
    val canScheduleExactAlarms: Boolean,
    val hasNotificationPermissions: Boolean,
    val isAlarmActive: Boolean,
    val alarmStorageInfo: AlarmStorageInfo?,
)
