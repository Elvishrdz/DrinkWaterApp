package com.eahm.drinkwaterapp.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class OnboardingViewModel(
    private val prefs: PrefsStorage,
    private val database: AppDatabase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingFormState())
    val state: StateFlow<OnboardingFormState> = _state.asStateFlow()

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onGenderSelect(gender: Gender) = _state.update { it.copy(gender = gender) }
    fun onWeightChange(value: String) = _state.update { it.copy(weightValue = value) }
    fun onWeightUnitChange(unit: WeightUnit) = _state.update { it.copy(weightUnit = unit) }
    fun onActivitySelect(level: ActivityLevel) = _state.update { it.copy(activityLevel = level) }
    fun onWakeTimeChange(time: OnbTimeSelection) = _state.update { it.copy(wakeTime = time) }
    fun onSleepTimeChange(time: OnbTimeSelection) = _state.update { it.copy(sleepTime = time) }
    fun onIntervalSelect(minutes: Int) = _state.update { it.copy(intervalMinutes = minutes) }

    fun computeGoal() {
        val s = _state.value
        _state.update {
            it.copy(
                computedGoalMl = calcGoalMl(
                    s.weightValue,
                    s.weightUnit,
                    s.activityLevel
                )
            )
        }
    }

    fun nextStep() = _state.update { it.copy(step = it.step + 1) }
    fun prevStep() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }

    fun finishOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            if (s.name.isNotBlank()) prefs.setUsername(s.name.trim())
            prefs.setStartTime(s.wakeTime.toActiveTime())
            prefs.setEndTime(s.sleepTime.toActiveTime())
            prefs.setInterval(
                ActiveTime(
                    hour = s.intervalMinutes / 60,
                    minute = s.intervalMinutes % 60,
                )
            )
            database.getDailyGoalDao().insert(
                DailyGoalEntity(
                    effectiveDate = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date,
                    goalMl = s.computedGoalMl.toFloat(),
                )
            )
            prefs.setFirstSetupDone(true)
            onComplete()
        }
    }

    private fun calcGoalMl(weightValue: String, unit: WeightUnit, activity: ActivityLevel?): Int {
        val kg = when (unit) {
            WeightUnit.LB -> (weightValue.toFloatOrNull() ?: 70f) * 0.453592f
            WeightUnit.KG -> weightValue.toFloatOrNull() ?: 70f
        }
        val multiplier = when (activity) {
            ActivityLevel.LIGHT -> 1.0f
            ActivityLevel.MODERATE -> 1.15f
            ActivityLevel.ACTIVE -> 1.30f
            null -> 1.0f
        }
        return (kg * 33f * multiplier).toInt().coerceIn(1000, 6000)
    }
}
