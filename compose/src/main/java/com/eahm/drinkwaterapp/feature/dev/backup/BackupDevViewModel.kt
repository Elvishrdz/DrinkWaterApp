package com.eahm.drinkwaterapp.feature.dev.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.backup.data.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.data.SyncConflictResolver
import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.GoalRecord
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
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
) : ViewModel() {

    // Accumulated merge stats across all sync calls in this session
    private val _mergedRecordCount = MutableStateFlow(0)
    private val _tombstonesApplied = MutableStateFlow(0)
    private val _autoResolvedConflicts = MutableStateFlow(0)

    private val _events = Channel<BackupDevEvent>(Channel.BUFFERED)
    val events: Flow<BackupDevEvent> = _events.receiveAsFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    private val _backupPreview = MutableStateFlow<String?>(null)

    private val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = BackupDevState(),
    )

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
            BackupDevAction.ClearLog -> _log.value = emptyList()
        }
    }

    private fun connectProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Connect ${type.displayName}…")
        val result = connectUseCase(type)
        log("${type.displayName} connect → $result")
    }

    private fun syncProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Sync ${type.displayName}…")
        val result = syncUseCase(registry.get(type))
        log("${type.displayName} sync → $result")
    }

    private fun restoreProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Restore ${type.displayName}…")
        val result = restoreUseCase(registry.get(type))
        log("${type.displayName} restore → $result")
    }

    private fun checkStatus(type: CloudProviderType) = viewModelScope.launch {
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
    private fun deleteRemoteFile(type: CloudProviderType) = viewModelScope.launch {
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
    private fun visualizeBackup(type: CloudProviderType) = viewModelScope.launch {
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

    private fun migrate(from: CloudProviderType, to: CloudProviderType) = viewModelScope.launch {
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
}
