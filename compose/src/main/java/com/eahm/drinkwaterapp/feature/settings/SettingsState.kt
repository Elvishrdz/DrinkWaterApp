package com.eahm.drinkwaterapp.feature.settings

import com.eahm.drinkwaterapp.core.data.datastore.ActiveTime

data class SettingsState(
    val username: String?,
    val weightKg: Float = 68f,
    val dailyGoalMl: Int = 2000,
    val unit: WaterUnit = WaterUnit.ML,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val notificationsEnabled: Boolean = false,
    val intervalMinutes: Int = 45,
    val startTime: ActiveTime?,
    val endTime: ActiveTime?,
    val timeRangeError: TimeRangeError? = null,
)

enum class WaterUnit(val label: String) {
    ML("Milliliters"),
    FL_OZ("Fl. oz"),
}

enum class WeightUnit(val label: String) {
    KG("kg"),
    LBS("lbs"),
}

enum class TimeRangeError {
    SAME_TIME,
}
