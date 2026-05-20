package com.eahm.drinkwaterapp.feature.settings

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService
import com.eahm.drinkwaterapp.analytics.tracking.TrackingService.Companion.OptOutSource
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkAlarmManagerV2
import com.eahm.drinkwaterapp.core.data.notificationpermission.NotificationPermissionObserver
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.dao.DEFAULT_GOAL_ML
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.drink.DateTimeZoneChangedObserver
import com.eahm.drinkwaterapp.feature.settings.data.ActiveTimes
import com.eahm.drinkwaterapp.ui.inputfield.validateText
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import com.revenuecat.purchases.Package as RcPackage

@OptIn(ExperimentalTime::class)
internal class SettingsViewModel(
    val application: Application,
    val database: AppDatabase,
    val prefsStorage: PrefsStorage,
    val drinkAlarmManagerV2: DrinkAlarmManagerV2,
    dateTimeZoneChangedObserver: DateTimeZoneChangedObserver,
    notificationPermissionObserver: NotificationPermissionObserver,
    appInfoService: AppInfoService,
    private val purchaseManager: PurchaseManager,
    private val trackingService: TrackingService,
) : ViewModel() {

    private val dao = database.getDailyGoalDao()

    private val activeTimes = combine(
        prefsStorage.getStartTime(),
        prefsStorage.getEndTime(),
    ) { startTime, endTime ->
        ActiveTimes(startTime, endTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val reminderInfo = combine(
        activeTimes,
        prefsStorage.getInterval(),
        prefsStorage.getAmountPerDrink(),
    ) { activeTimes, interval, notificationAmountMl ->
        Triple(activeTimes, interval, notificationAmountMl)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private val canDrawOverlay get() = Settings.canDrawOverlays(application)

    private val canScheduleExactAlarms: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == true
        } else {
            true
        }

    private val _state =
        MutableStateFlow(
            SettingsState(
                username = null,
                notificationsEnabled = notificationPermissionObserver.hasPermission.value,
                startTime = null,
                endTime = null,
                intervalMinutes = 0,
                goalReachedCelebrationsEnabled = canDrawOverlay,
                exactAlarmsEnabled = canScheduleExactAlarms,
                isTrackingEnabled = trackingService.isTracking(),
                appVersion = appInfoService.versionName,
                buildVariant = appInfoService.buildVariant,
            )
        )

    val today = dateTimeZoneChangedObserver.currentDateTime

    @OptIn(ExperimentalCoroutinesApi::class)
    private val goal = dateTimeZoneChangedObserver.currentDateTime
        .flatMapLatest { today ->
            dao.getGoalForDate(today.date.toEpochDays())
        }
        .map { (it?.goalMl ?: DEFAULT_GOAL_ML) }
        .flowOn(Dispatchers.IO)

    val state = combine(
        prefsStorage.getUsername(),
        goal,
        reminderInfo,
        notificationPermissionObserver.hasPermission,
        _state,
    ) { username, goal, reminderInfo, hasNotificationPermission, state ->
        state.copy(
            username = username,
            dailyGoalMl = goal,
            notificationsEnabled = hasNotificationPermission,
            intervalMinutes = reminderInfo?.second?.let { it.hour * 60 + it.minute } ?: 0,
            startTime = reminderInfo?.first?.start,
            endTime = reminderInfo?.first?.end,
            notificationAmountMl = reminderInfo?.third ?: 150f,
        )
    }
        .combine(purchaseManager.isAdFree) { state, isAdFree ->
            state.copy(isAdFree = isAdFree)
        }
        .combine(purchaseManager.purchaseState) { state, purchaseState ->
            state.copy(purchaseState = purchaseState)
        }
        .combine(prefsStorage.getTimeFormatPreference()) { state, timeFormatPreference ->
            state.copy(timeFormatPreference = timeFormatPreference)
        }
        .combine(prefsStorage.getUnitPreference()) { state, unitPref ->
            val rawTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            state.copy(
                unitPreference = unitPref,
                currentLocaleTag = if (rawTag == "und" || rawTag.isBlank()) "" else rawTag,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = _state.value,
        )

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun onUsernameChanged(username: String) {
        viewModelScope.launch {
            username
                .takeIf { validateText(it).isValid }
                ?.let { prefsStorage.setUsername(it) }
        }
    }

    fun onGoalChanged(goalMl: Float) {
        viewModelScope.launch {
            setGoalFromToday(goalMl)
            drinkAlarmManagerV2.restoreAlarm(source = "settings-goal-changed")
        }
    }

    fun onUnitChanged(unit: WaterUnit, weightUnit: WeightUnit) {
        _state.update { it.copy(unit = unit, weightUnit = weightUnit) }
    }

    fun onIntervalChanged(minutes: Int) {
        viewModelScope.launch {
            _state.update { it.copy(intervalMinutes = minutes) }
            prefsStorage.setInterval(ActiveTime(hour = minutes / 60, minute = minutes % 60))
            drinkAlarmManagerV2.restoreAlarm(source = "settings-interval-changed")
        }
    }

    fun onNotificationAmountChanged(ml: Float) {
        viewModelScope.launch {
            prefsStorage.setAmountPerDrink(ml)
        }
    }

    fun onStartTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            val newStart = ActiveTime(time.hour, time.minute)
            val currentEnd = state.value.endTime

            val error = currentEnd?.let { validateTimeRange(newStart, it) }
            if (error != null) {
                _state.update { it.copy(timeRangeError = error) }
                return@launch
            }

            _state.update { it.copy(timeRangeError = null) }
            prefsStorage.setStartTime(newStart)
            drinkAlarmManagerV2.restoreAlarm(source = "settings-start-time-changed")
        }
    }

    fun onEndTimeChanged(time: TimeSelected) {
        viewModelScope.launch {
            val newEnd = ActiveTime(time.hour, time.minute)
            val currentStart = state.value.startTime

            val error = currentStart?.let { validateTimeRange(it, newEnd) }
            if (error != null) {
                _state.update { it.copy(timeRangeError = error) }
                return@launch
            }

            _state.update { it.copy(timeRangeError = null) }
            prefsStorage.setEndTime(newEnd)
            drinkAlarmManagerV2.restoreAlarm(source = "settings-end-time-changed")
        }
    }

    fun onTimeFormatPreferenceChanged(pref: TimeFormatPreference) {
        viewModelScope.launch { prefsStorage.setTimeFormatPreference(pref) }
    }

    fun onUnitPreferenceChanged(pref: UnitPreference) {
        viewModelScope.launch { prefsStorage.setUnitPreference(pref) }
    }

    fun onLanguageChanged(tag: String) {
        val locales = if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
        application
            .getSharedPreferences("locale_prefs", android.content.Context.MODE_PRIVATE)
            .edit { putString("locale_tag", tag) }
    }

    fun onDrawOverlayPermissionResult() {
        _state.update { it.copy(goalReachedCelebrationsEnabled = canDrawOverlay) }
    }

    fun onExactAlarmPermissionResult() {
        _state.update { it.copy(exactAlarmsEnabled = canScheduleExactAlarms) }
    }

    fun onTrackingToggled(enabled: Boolean) {
        if (enabled) {
            trackingService.startTracking()
        } else {
            trackingService.stopTracking(OptOutSource.Settings)
        }
        _state.update { it.copy(isTrackingEnabled = enabled) }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.getHistoryDao().deleteAll()
            database.getDailyGoalDao().deleteAll()
            prefsStorage.clear()
            drinkAlarmManagerV2.clear()

            // restart application after cleanup
            application.packageManager
                .getLaunchIntentForPackage(application.packageName)
                ?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ?.let {
                    val mainIntent = Intent.makeRestartActivityTask(it.component)
                    application.startActivity(mainIntent)
                    Runtime.getRuntime().exit(0)
                }
        }
    }

    fun onFetchOfferings() {
        _state.update { it.copy(offeringsState = OfferingsState.Loading) }
        purchaseManager.fetchOfferings(
            onSuccess = { pkg ->
                _state.update { it.copy(offeringsState = OfferingsState.Success(pkg)) }
            },
            onError = {
                _state.update { it.copy(offeringsState = OfferingsState.Error) }
            },
        )
    }

    fun onOfferingsSheetDismissed() {
        purchaseManager.cancel()
        _state.update { it.copy(offeringsState = null) }
    }

    fun onPurchase(activity: Activity, pkg: RcPackage) {
        purchaseManager.purchase(activity, pkg)
    }

    fun onRestorePurchase() {
        purchaseManager.restore()
    }

    private suspend fun setGoalFromToday(goalMl: Float) {
        val current = today.value.date
        val epochDay = current.toEpochDays()
        val existing = dao.getGoalOnDate(epochDay).first()
        if (existing != null) dao.update(existing.copy(goalMl = goalMl))
        else dao.insert(DailyGoalEntity(effectiveDate = current, goalMl = goalMl))
    }

    /**
     * Returns a [TimeRangeError] if the [start]/[end] pair is invalid, null if the range is fine.
     *
     * Rules:
     * - [start] == [end] (same minute) → [TimeRangeError.SAME_TIME] — zero-duration window.
     * - [start] < [end] → normal same-day window, always valid.
     * - [start] > [end] → midnight-spanning window (e.g. 23:00 → 02:00), always valid.
     */
    private fun validateTimeRange(start: ActiveTime, end: ActiveTime): TimeRangeError? {
        val startMinutes = start.hour * 60 + start.minute
        val endMinutes = end.hour * 60 + end.minute
        return if (startMinutes == endMinutes) TimeRangeError.SAME_TIME else null
    }
}
