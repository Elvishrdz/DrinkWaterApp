package com.eahm.drinkwaterapp.core.data.drinkalarm.data

data class AlarmConfiguration(
    val source: AlarmSource,
    val schedule: AlarmSchedule,
)

sealed interface AlarmSource {
    data object OnboardingCompleted : AlarmSource
    data object DrinkAlarmReceived : AlarmSource
    data object DrinkButton : AlarmSource
    data object NotificationDrinkButton : AlarmSource
    data object NotificationSkipButton : AlarmSource
    data object NotificationRemoved : AlarmSource
    data object NotificationActiveHoursStartTimeChanged : AlarmSource
    data object NotificationActiveHoursEndTimeChanged : AlarmSource
    data object DrinkButtonDevMenu : AlarmSource
    data object AlarmsDevMenu : AlarmSource
    data class CustomSource(val source: String) : AlarmSource
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
