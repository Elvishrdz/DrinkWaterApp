package com.eahm.drinkwaterapp.core.data.drinkalarm

data class DrinkAlarmState(
    val active: Boolean,
    val interval: Long?,
    val remainingTime: String,
)
