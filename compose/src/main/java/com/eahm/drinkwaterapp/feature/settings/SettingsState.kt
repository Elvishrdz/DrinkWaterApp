package com.eahm.drinkwaterapp.feature.settings

data class SettingsState(
    val userName: String = "Alex",
    val weightKg: Float = 68f,
    val dailyGoalMl: Int = 2200,
    val unit: WaterUnit = WaterUnit.ML,
    val notificationsEnabled: Boolean = true,
    val intervalMinutes: Int = 45,
    val startHour: Int = 6,
    val startMinute: Int = 5,
    val endHour: Int = 23,
    val endMinute: Int = 55,
)

enum class WaterUnit(val label: String) {
    ML("Milliliters"),
    FL_OZ("Fl. oz"),
}
