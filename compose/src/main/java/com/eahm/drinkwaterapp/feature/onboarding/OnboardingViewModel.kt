package com.eahm.drinkwaterapp.feature.onboarding

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.backup.data.currentBackupSchedule
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.domain.extension.canScheduleExactAlarms
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class OnboardingViewModel(
    private val application: Application,
    private val prefs: PrefsStorage,
    database: AppDatabase,
    private val drinkAlarmManagerV2: DrinkAlarmManagerV2,
    private val tracking: TrackingService,
    private val backupPreferences: BackupPreferences,
    private val backupScheduler: BackupScheduler,
) : ViewModel() {

    private val dailyGoalDao = database.getDailyGoalDao()

    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private var onboardingCompleted = false

    init {
        tracking.startTrackTimeOnboardingTime()
        tracking.trackOnboardingStarted()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val goal = dailyGoalDao.getGoalForDate(today.date.toEpochDays())
        .map { (it?.goalMl ?: DEFAULT_GOAL_ML) }
        .flowOn(Dispatchers.IO)

    private val _state = MutableStateFlow(OnboardingFormState(goalMl = DEFAULT_GOAL_ML))
    val state: StateFlow<OnboardingFormState> = combine(
        _state,
        goal,
    ) { state, goal ->
        state.copy(
            goalMl = goal,
        )
    }.combine(prefs.getTimeFormatPreference()) { state, timeFormat ->
        state.copy(timeFormatPreference = timeFormat)
    }.combine(prefs.getUnitPreference()) { state, unitPref ->
        state.copy(unitPreference = unitPref)
    }.combine(prefs.getStartTime()) { state, start ->
        state.copy(wakeTime = start.toTimeSelection)
    }.combine(prefs.getEndTime()) { state, end ->
        state.copy(sleepTime = end.toTimeSelection)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed((5_000)),
        initialValue = _state.value,
    )

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onGenderSelect(gender: Gender) = _state.update { it.copy(gender = gender) }
    fun onWeightChange(value: String) = _state.update { it.copy(weightValue = value) }
    fun onWeightUnitChange(unit: WeightUnit) = _state.update { it.copy(weightUnit = unit) }
    fun onActivitySelect(level: ActivityLevel) = _state.update { it.copy(activityLevel = level) }

    fun onWakeTimeChange(time: OnbTimeSelection) {
        viewModelScope.launch { prefs.setStartTime(time.toActiveTime()) }
    }

    fun onSleepTimeChange(time: OnbTimeSelection) {
        viewModelScope.launch { prefs.setEndTime(time.toActiveTime()) }
    }

    fun onIntervalSelect(minutes: Int) {
        _state.update { it.copy(intervalMinutes = minutes) }
        viewModelScope.launch {
            prefs.setInterval(ActiveTime(hour = minutes / 60, minute = minutes % 60))
        }
    }

    fun onUnitPreferenceSelect(pref: UnitPreference) {
        _state.update { it.copy(unitPreference = pref) }
        viewModelScope.launch { prefs.setUnitPreference(pref) }
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

    fun nextStep() = _state.update { it.copy(stepNumber = it.stepNumber + 1) }
    fun prevStep() = _state.update { it.copy(stepNumber = (it.stepNumber - 1).coerceAtLeast(1)) }

    fun onCloudBackupSkipConfiguredStep() {
        val nextStep = if (!application.canShowNotifications) {
            OnboardingSteps.RemindersSteps.Notification
        } else if (!application.canScheduleExactAlarms) {
            OnboardingSteps.RemindersSteps.ExactAlarm
        } else if (!Settings.canDrawOverlays(application)) {
            OnboardingSteps.Celebrations
        } else {
            OnboardingSteps.Summary
        }

        _state.update { it.copy(stepNumber = OnboardingSteps.getStepNumberByStep(nextStep)) }
    }

    fun finishOnboarding(onComplete: () -> Unit) {
        onboardingCompleted = true
        viewModelScope.launch {
            tracking.trackOnboardingCompleted()
            tracking.stopTrackTimeOnboardingTime()
            prefs.setOnboardingCompleted(true)
            drinkAlarmManagerV2.restoreAlarm(source = "onboarding")

            val providerId = backupPreferences.getBackupProvider()
            val providerType = providerId?.let { CloudProviderType.fromId(it) }
            if (providerType != null) {
                backupScheduler.triggerManualSync()
                backupScheduler.scheduleAutoSync(backupPreferences.currentBackupSchedule())
            }

            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!onboardingCompleted) {
            val stepName =
                OnboardingSteps.getStepByNumber(_state.value.stepNumber)::class.simpleName
                    ?: "Unknown"
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
