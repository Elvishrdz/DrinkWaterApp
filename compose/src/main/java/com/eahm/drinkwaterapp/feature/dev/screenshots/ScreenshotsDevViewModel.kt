@file:Suppress("MagicNumber")

package com.eahm.drinkwaterapp.feature.dev.screenshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.config.AppInfoService
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import com.eahm.drinkwaterapp.core.database.AppDatabase
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.screenshot.ScreenshotModeController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ScreenshotsDevViewModel(
    database: AppDatabase,
    private val prefsStorage: PrefsStorage,
    private val screenshotModeController: ScreenshotModeController,
    appInfoService: AppInfoService,
) : ViewModel() {

    private val historyDao = database.getHistoryDao()
    private val goalDao = database.getDailyGoalDao()

    /** KMP note: swap for the shared `kmpQueryContext` expect/actual when this moves to `:shared`. */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    /** Local UI state (version field, job status) combined with the persisted screenshot-mode state. */
    private val local = MutableStateFlow(
        LocalState(version = appInfoService.versionName.substringBefore('-')),
    )

    val state = combine(local, screenshotModeController.state) { local, mode ->
        ScreenshotsDevState.Data(
            version = local.version,
            screenshotModeEnabled = mode.enabled,
            isRunning = local.isRunning,
            resultMessage = local.resultMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ScreenshotsDevState.Loading,
    )

    fun onVersionChanged(version: String) = local.update { it.copy(version = version) }

    /** Switches the app into release-like screenshot presentation using the entered version. */
    fun setForScreenshots() {
        viewModelScope.launch {
            screenshotModeController.enable(local.value.version.trim())
            local.update { it.copy(resultMessage = "Screenshot mode ON — disable via 10 ring taps") }
        }
    }

    /** Inserts the hand-authored [screenshotPreset] anchored behind today + applies reminder config. */
    fun insertPreset() {
        if (local.value.isRunning) return
        viewModelScope.launch {
            val history = screenshotPreset.history.map { h ->
                HistoryEntity(
                    amount = h.amountMl,
                    status = h.status,
                    timestamp = LocalDateTime(
                        today.minus(h.daysAgo, DateTimeUnit.DAY),
                        LocalTime(h.hour, h.minute),
                    ),
                )
            }
            val goals = screenshotPreset.goals.map { g ->
                DailyGoalEntity(
                    effectiveDate = today.minus(g.daysAgo, DateTimeUnit.DAY),
                    goalMl = g.goalMl,
                )
            }
            applyReminders(screenshotPreset.reminders)
            runJob(history, goals, "Inserted preset + reminder config")
        }
    }

    fun clearAll() {
        if (local.value.isRunning) return
        viewModelScope.launch {
            local.update { it.copy(isRunning = true, resultMessage = "Clearing…") }
            withContext(ioDispatcher) {
                historyDao.deleteAll()
                goalDao.deleteAll()
            }
            local.update {
                it.copy(isRunning = false, resultMessage = "All history and goal records cleared")
            }
        }
    }

    private suspend fun applyReminders(reminders: PresetReminders) {
        prefsStorage.setStartTime(reminders.startTime)
        prefsStorage.setEndTime(reminders.endTime)
        prefsStorage.setInterval(reminders.interval)
        prefsStorage.setAmountPerDrink(reminders.amountPerDrinkMl)
        prefsStorage.setActiveDays(reminders.activeDays)
    }

    private suspend fun runJob(
        history: List<HistoryEntity>,
        goals: List<DailyGoalEntity>,
        successLabel: String,
    ) {
        local.update { it.copy(isRunning = true, resultMessage = "Inserting…") }
        withContext(ioDispatcher) {
            historyDao.deleteAll()
            goalDao.deleteAll()
            history.chunked(CHUNK_SIZE)
                .forEach { chunk -> historyDao.insertAll(*chunk.toTypedArray()) }
            goals.forEach { goalDao.insert(it) }
        }
        local.update {
            it.copy(
                isRunning = false,
                resultMessage = "$successLabel — ${history.size} drinks, ${goals.size} goals",
            )
        }
    }

    private data class LocalState(
        val version: String,
        val isRunning: Boolean = false,
        val resultMessage: String = "",
    )

    private companion object {
        const val CHUNK_SIZE = 500
    }
}
