package com.eahm.drinkwaterapp.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.TrackingService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManager
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.AlarmSource
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    private val drinkAlarmManager: DrinkAlarmManager,
    private val tracking: TrackingService,
) : ViewModel() {

    private val dailyGoalDao = database.getDailyGoalDao()

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private var onboardingCompleted = false

    init {
        tracking.startTrackTimeOnboardingTime()
        tracking.trackOnboardingStarted()
    }

    private val _state = MutableStateFlow(OnboardingFormState(goalMl = DEFAULT_GOAL_ML))
    val state: StateFlow<OnboardingFormState> = combine(
        _state,
        dailyGoalDao.getGoalOnDate(today.date.toEpochDays()),
    ) { state, goal ->
        state.copy(
            goalMl = goal?.goalMl ?: DEFAULT_GOAL_ML,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        _state.value,
    )

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onGenderSelect(gender: Gender) = _state.update { it.copy(gender = gender) }
    fun onWeightChange(value: String) = _state.update { it.copy(weightValue = value) }
    fun onWeightUnitChange(unit: WeightUnit) = _state.update { it.copy(weightUnit = unit) }
    fun onActivitySelect(level: ActivityLevel) = _state.update { it.copy(activityLevel = level) }
    fun onWakeTimeChange(time: OnbTimeSelection) {
        _state.update { it.copy(wakeTime = time) }
        viewModelScope.launch { prefs.setStartTime(time.toActiveTime()) }
    }

    fun onSleepTimeChange(time: OnbTimeSelection) {
        _state.update { it.copy(sleepTime = time) }
        viewModelScope.launch { prefs.setEndTime(time.toActiveTime()) }
    }

    fun onIntervalSelect(minutes: Int) {
        _state.update { it.copy(intervalMinutes = minutes) }
        viewModelScope.launch {
            prefs.setInterval(ActiveTime(hour = minutes / 60, minute = minutes % 60))
        }
    }

    fun onNotifAmountChange(ml: Float) {
        _state.update { it.copy(notifAmountMl = ml) }
        viewModelScope.launch { prefs.setAmountPerDrink(ml) }
    }

    fun onGoalChanged(goalMl: Float) {
        viewModelScope.launch {
            upsertGoalForToday(goalMl)
        }
    }

    fun computeGoal() {
        // TODO: when weight is received.
        val s = _state.value
        _state.update {
            it.copy(
                goalMl = calcGoalMl(
                    s.weightValue,
                    s.weightUnit,
                    s.activityLevel
                ).toFloat(),
            )
        }
    }

    fun nextStep() = _state.update { it.copy(step = it.step + 1) }
    fun prevStep() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }

    fun finishOnboarding(onComplete: () -> Unit) {
        onboardingCompleted = true
        viewModelScope.launch {
            tracking.trackOnboardingCompleted()
            tracking.stopTrackTimeOnboardingTime()

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
            upsertGoalForToday(s.goalMl)
            prefs.setAmountPerDrink(s.notifAmountMl)
            prefs.setOnboardingCompleted(true)
            drinkAlarmManager.determineAction(AlarmSource.OnboardingCompleted)
            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!onboardingCompleted) {
            val stepName = OnboardingSteps.byStep(_state.value.step)::class.simpleName ?: "Unknown"
            tracking.trackOnboardingAbandoned(stepName)
        }
        tracking.stopTrackTimeOnboardingTime()
    }

    private suspend fun upsertGoalForToday(goalMl: Float) {
        val current = today.date
        val epochDay = current.toEpochDays()
        val existing = dailyGoalDao.getGoalOnDate(epochDay).first()
        if (existing != null) dailyGoalDao.update(existing.copy(goalMl = goalMl))
        else dailyGoalDao.insert(DailyGoalEntity(effectiveDate = current, goalMl = goalMl))
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
