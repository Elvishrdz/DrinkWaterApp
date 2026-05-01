package com.eahm.drinkwaterapp.feature.settings

import com.eahm.drinkwaterapp.core.data.datastore.ActiveTime

data class SettingsState(
    val username: String?,
    val weightKg: Float = 68f,
    val dailyGoalMl: Int = 2200,
    val unit: WaterUnit = WaterUnit.ML,
    val notificationsEnabled: Boolean = true,
    val intervalMinutes: Int = 45,
    val startTime: ActiveTime?,
    val endTime: ActiveTime?,
)

enum class WaterUnit(val label: String) {
    ML("Milliliters"),
    FL_OZ("Fl. oz"),
}
