package com.eahm.drinkwaterapp.feature.settings

import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.core.billing.PurchaseState
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.revenuecat.purchases.Package as RcPackage

@Immutable
sealed interface OfferingsState {

    data object Loading : OfferingsState
    data class Success(val pkg: RcPackage?) : OfferingsState
    data object Error : OfferingsState
}

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
    val exactAlarmsEnabled: Boolean = false,
    val notificationAmountMl: Float = 150f,
    val appVersion: String,
    val buildVariant: String,
    val isAdFree: Boolean = false,
    val purchaseState: PurchaseState = PurchaseState.Idle,
    val offeringsState: OfferingsState? = null,
    val timeFormatPreference: TimeFormatPreference = TimeFormatPreference.SYSTEM,
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
