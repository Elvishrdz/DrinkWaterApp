package com.eahm.drinkwaterapp.core.data.drinkalarm

import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval

data class DrinkAlarmState(
    val active: Boolean,
    val interval: AlarmInterval,
    val nextInterval: AlarmInterval?,
)
