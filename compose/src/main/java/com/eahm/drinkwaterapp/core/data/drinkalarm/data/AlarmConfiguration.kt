package com.eahm.drinkwaterapp.core.data.drinkalarm.data

data class AlarmConfiguration(
    val source: AlarmSource,
    val schedule: AlarmSchedule,
)

enum class AlarmSource {
    DrinkAlarmReceived,
    DrinkButton,
    NotificationDrinkButton,
    NotificationSkipButton,
    NotificationRemoved,
    DrinkButtonDevMenu,
    AlarmsDevMenu,
}

sealed interface AlarmSchedule {

    data class AtExactTime(
        val hour: Int,
        val minute: Int,
        val second: Int,
    ) : AlarmSchedule

    data class AfterGivenTime(
        val milliseconds: Long,
    ) : AlarmSchedule
}
