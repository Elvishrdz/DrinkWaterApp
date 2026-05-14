package com.eahm.drinkwaterapp.feature.settings

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML

data class SettingsState(
    val username: String?,
    val weightKg: Float = 68f,
    val dailyGoalMl: Float = DEFAULT_GOAL_ML,
    val unit: WaterUnit = WaterUnit.ML,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val notificationsEnabled: Boolean,
    val intervalMinutes: Int,
    val startTime: ActiveTime?,
    val endTime: ActiveTime?,
    val timeRangeError: TimeRangeError? = null,
    val goalReachedCelebrationsEnabled: Boolean,
    val notificationAmountMl: Float = 150f,
    val appVersion: String,
    val buildVariant: String,
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
