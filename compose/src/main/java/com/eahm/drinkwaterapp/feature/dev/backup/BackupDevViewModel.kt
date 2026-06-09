package com.eahm.drinkwaterapp.feature.dev.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.eahm.drinkwaterapp.backup.data.BackupChangeTracker
import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.currentBackupSchedule
import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.data.worker.CloudBackupWorker
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.BackupScheduler
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.BackupSyncStatus
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
import com.eahm.drinkwaterapp.core.backup.BackupNotificationMode
import com.eahm.drinkwaterapp.core.backup.BackupSyncNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("LongParameterList")
@OptIn(ExperimentalTime::class)
internal class BackupDevViewModel(
    private val registry: BackupProviderRegistry,
    private val connectUseCase: ConnectBackupUseCase,
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val checkStatusUseCase: CheckBackupStatusUseCase,
    private val disconnectUseCase: DisconnectBackupUseCase,
    private val migrateUseCase: MigrateBackupUseCase,
    private val repository: BackupRepository,
    private val conflictResolver: SyncConflictResolver,
    private val backupPreferences: BackupPreferences,
    private val scheduler: BackupScheduler,
    private val workManager: WorkManager,
    private val backupSyncNotifier: BackupSyncNotifier,
    private val changeTracker: BackupChangeTracker,
) : ViewModel() {

    // Accumulated merge stats across all sync calls in this session
    private val _mergedRecordCount = MutableStateFlow(0)
    private val _tombstonesApplied = MutableStateFlow(0)
    private val _autoResolvedConflicts = MutableStateFlow(0)

    private val _events = Channel<BackupDevEvent>(Channel.BUFFERED)
    val events: Flow<BackupDevEvent> = _events.receiveAsFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    private val _backupPreview = MutableStateFlow<String?>(null)
    private val _notificationMode = MutableStateFlow(BackupNotificationMode.OFF)

    private val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    /** Live WorkManager state for each unique backup work name, summarised for display. */
    private val workSummary = combine(
        workManager.getWorkInfosForUniqueWorkFlow(CloudBackupWorker.WORK_NAME_PERIODIC),
        workManager.getWorkInfosForUniqueWorkFlow(CloudBackupWorker.WORK_NAME_MANUAL),
        workManager.getWorkInfosForUniqueWorkFlow(CloudBackupWorker.WORK_NAME_ON_CHANGE),
        workManager.getWorkInfosForUniqueWorkFlow(CloudBackupWorker.WORK_NAME_FOREGROUND),
    ) { periodic, manual, onChange, foreground ->
        buildString {
            appendLine("Enqueued works: ${enqueuedCount(periodic, manual, onChange, foreground)}")
            appendLine("daily:      ${describe(periodic)}")
            appendLine("manual:     ${describe(manual)}")
            appendLine("on-change:  ${describe(onChange)}")
            append("foreground: ${describe(foreground)}")
        }
    }

    val state = combine(
        _log,
        repository.syncState,
    ) { log, syncState ->
        BackupDevState(syncState = syncState, log = log)
    }.combine(_mergedRecordCount) { state, merged ->
        state.copy(mergedRecordCount = merged)
    }.combine(_tombstonesApplied) { state, tombstones ->
        state.copy(tombstonesApplied = tombstones)
    }.combine(_autoResolvedConflicts) { state, autoResolved ->
        state.copy(autoResolvedConflicts = autoResolved)
    }.combine(_backupPreview) { state, preview ->
        state.copy(backupPreview = preview)
    }.combine(repository.syncStatus) { state, status ->
        state.copy(syncStatusLabel = describeStatus(status))
    }.combine(workSummary) { state, summary ->
        state.copy(workInfoSummary = summary)
    }.combine(_notificationMode) { state, mode ->
        state.copy(notificationMode = mode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = BackupDevState(),
    )

    init {
        viewModelScope.launch {
            _notificationMode.value = backupSyncNotifier.currentMode()
        }
    }

    fun onAction(action: BackupDevAction) {
        when (action) {
            BackupDevAction.ConnectGoogle -> connectProvider(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.SyncGoogle -> syncProvider(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.RestoreGoogle -> restoreProvider(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.CheckStatusGoogle -> checkStatus(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.DisconnectGoogle -> disconnectProvider(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.DeleteFileGoogle -> deleteRemoteFile(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.VisualizeGoogle -> visualizeBackup(CloudProviderType.GOOGLE_DRIVE)
            BackupDevAction.ConnectDropbox -> connectProvider(CloudProviderType.DROPBOX)
            BackupDevAction.SyncDropbox -> syncProvider(CloudProviderType.DROPBOX)
            BackupDevAction.RestoreDropbox -> restoreProvider(CloudProviderType.DROPBOX)
            BackupDevAction.CheckStatusDropbox -> checkStatus(CloudProviderType.DROPBOX)
            BackupDevAction.DisconnectDropbox -> disconnectProvider(CloudProviderType.DROPBOX)
            BackupDevAction.DeleteFileDropbox -> deleteRemoteFile(CloudProviderType.DROPBOX)
            BackupDevAction.VisualizeDropbox -> visualizeBackup(CloudProviderType.DROPBOX)
            BackupDevAction.DismissBackupPreview -> _backupPreview.value = null
            BackupDevAction.MigrateGoogleToDropbox -> migrate(
                CloudProviderType.GOOGLE_DRIVE,
                CloudProviderType.DROPBOX,
            )

            BackupDevAction.MigrateDropboxToGoogle -> migrate(
                CloudProviderType.DROPBOX,
                CloudProviderType.GOOGLE_DRIVE,
            )

            BackupDevAction.SimulateGoalConflict -> simulateGoalConflict()
            BackupDevAction.SimulatePrefsConflict -> simulatePrefsConflict()
            BackupDevAction.SimulateTombstoneWarning -> simulateTombstoneWarning()

            BackupDevAction.RunBackgroundSyncNow -> {
                log("Enqueue background sync (manual one-time work)…")
                scheduler.triggerManualSync()
            }

            BackupDevAction.TriggerOnChangeSync -> {
                // Faithfully simulate a save: mark the change (so a sync running right now will
                // detect it and queue exactly one follow-up) then enqueue with KEEP.
                changeTracker.markChanged()
                log("Simulate save → markChanged + enqueue on-change (KEEP)…")
                scheduler.triggerOnChangeSync()
            }

            BackupDevAction.TriggerForegroundSync -> {
                log("Enqueue foreground sync…")
                scheduler.triggerForegroundSync()
            }

            BackupDevAction.ScheduleDailySync -> scheduleDailySync()
            BackupDevAction.CancelAllWork -> {
                log("Cancel all backup work…")
                scheduler.cancelAll()
            }

            is BackupDevAction.SetNotificationMode -> setNotificationMode(action.mode)

            BackupDevAction.ClearLog -> _log.value = emptyList()
        }
    }

    private fun scheduleDailySync() = viewModelScope.launch {
        val schedule = backupPreferences.currentBackupSchedule()
        scheduler.scheduleAutoSync(schedule)
        log("Scheduled auto-sync: $schedule")
    }

    private fun setNotificationMode(mode: BackupNotificationMode) = viewModelScope.launch {
        backupSyncNotifier.setMode(mode)
        _notificationMode.value = mode
        log("Backup status notification → ${mode.name}")
    }

    private fun connectProvider(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Connect ${type.displayName}…")
        val result = connectUseCase(type)
        log("${type.displayName} connect → $result")
    }

    private fun syncProvider(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Sync ${type.displayName}…")
        val result = syncUseCase(registry.get(type))
        log("${type.displayName} sync → $result")
    }

    private fun restoreProvider(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Restore ${type.displayName}…")
        val result = restoreUseCase(registry.get(type))
        log("${type.displayName} restore → $result")
    }

    private fun checkStatus(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Check status ${type.displayName}…")
        val result = checkStatusUseCase(registry.get(type))
        log("${type.displayName} status → $result")
    }

    private fun disconnectProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Disconnect ${type.displayName}…")
        registry.get(type).signOut()
        log("${type.displayName} signed out")
    }

    /**
     * Deletes the backup file from the cloud provider and resets local sync bookkeeping
     * so the next sync behaves like a first-ever upload — i.e. as if the backup never
     * existed (fresh install).
     */
    private fun deleteRemoteFile(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Delete remote file ${type.displayName}…")
        when (val result = registry.get(type).deleteRemoteFile()) {
            is BackupResult.Success -> {
                backupPreferences.setBackupContentHash(null)
                backupPreferences.setSeenDevices(emptyList())
                backupPreferences.setSyncPending(false)
                log("${type.displayName} remote file deleted — local sync state reset")
            }

            is BackupResult.Failure ->
                log("${type.displayName} delete failed → ${result.error}")
        }
    }

    /**
     * Downloads the remote backup and shows its pretty-printed JSON in the viewer sheet.
     * On failure, logs why so the reason is visible in the log panel.
     */
    private fun visualizeBackup(type: CloudProviderType) = viewModelScope.launch(Dispatchers.IO) {
        log("Fetch backup file ${type.displayName}…")
        when (val result = registry.get(type).download()) {
            is BackupResult.Success -> {
                val pretty = runCatching {
                    prettyJson.encodeToString(JsonObject.serializer(), result.value.rawData)
                }.getOrElse { result.value.rawData.toString() }
                _backupPreview.value = pretty
                log("${type.displayName} backup loaded (${pretty.length} chars)")
            }

            is BackupResult.Failure ->
                log("${type.displayName} could not load backup → ${result.error}")
        }
    }

    private fun migrate(from: CloudProviderType, to: CloudProviderType) =
        viewModelScope.launch(Dispatchers.IO) {
            log("Migrate ${from.displayName} → ${to.displayName}…")
            val result = migrateUseCase(registry.get(from), registry.get(to))
            log("Migrate → $result")
        }

    private fun simulateGoalConflict() = viewModelScope.launch {
        log("[SIM] Injecting GoalConflict into SyncConflictResolver…")
        val todayEpochDay = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date.toEpochDays().toInt()
        val conflict = SyncConflict.GoalConflict(
            effectiveDate = todayEpochDay,
            localGoal = GoalRecord(
                id = "local-sim",
                effectiveDate = todayEpochDay,
                goalMl = 2000f,
            ),
            remoteGoal = GoalRecord(
                id = "remote-sim",
                effectiveDate = todayEpochDay,
                goalMl = 2500f,
            ),
        )
        val useRemote = conflictResolver.awaitResolution(conflict)
        log("[SIM] GoalConflict resolved → useRemote=$useRemote")
    }

    private fun simulatePrefsConflict() = viewModelScope.launch {
        log("[SIM] Injecting PreferencesConflict into SyncConflictResolver…")
        val conflict = SyncConflict.PreferencesConflict(
            localPrefs = PreferencesRecord(
                unitPreference = "ML",
                timeFormat = "24h",
                amountPerDrink = 250f,
            ),
            remotePrefs = PreferencesRecord(
                unitPreference = "OZ",
                timeFormat = "12h",
                amountPerDrink = 300f,
            ),
        )
        val useRemote = conflictResolver.awaitResolution(conflict)
        log("[SIM] PreferencesConflict resolved → useRemote=$useRemote")
    }

    private fun simulateTombstoneWarning() = viewModelScope.launch {
        log("[SIM] Setting tombstone warning pending flag…")
        backupPreferences.setTombstoneWarningPending(true)
        log("[SIM] Tombstone warning flag set — open the Today/Drink screen to see the dialog")
    }

    private fun log(message: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val ts = "%02d:%02d:%02d".format(now.hour, now.minute, now.second)
        _log.update { it + "[$ts] $message" }
    }

    private fun describe(infos: List<WorkInfo>): String {
        if (infos.isEmpty()) return "—"
        // Show every WorkInfo for this unique name so queued follow-ups (e.g. an appended
        // on-change sync waiting behind a running one) are visible, not just the first. Each entry
        // is a distinct work; index them (#1, #2) so two works aren't mistaken for one retried
        // work. "attempt" is that single work's retry count (0 = first run), not a work index.
        return infos.mapIndexed { index, info ->
            val label = if (infos.size > 1) "#${index + 1} " else ""
            val attempt = if (info.runAttemptCount > 0) " (attempt ${info.runAttemptCount})" else ""
            "$label${info.state}$attempt"
        }.joinToString(", ")
    }

    /** Count of WorkInfos currently in the ENQUEUED (queued, waiting to run) state. */
    private fun enqueuedCount(vararg lists: List<WorkInfo>): Int =
        lists.sumOf { list -> list.count { it.state == WorkInfo.State.ENQUEUED } }

    private fun describeStatus(status: BackupSyncStatus): String = when {
        status.running -> "Running (${status.activeTrigger?.name ?: "?"})"
        else -> when (val outcome = status.lastOutcome) {
            is BackupSyncStatus.Outcome.Success -> "Success (${outcome.trigger.name})"
            is BackupSyncStatus.Outcome.Failure ->
                "Failed (${outcome.trigger.name} → ${outcome.disposition.name})"

            null -> "Idle"
        }
    }
}
