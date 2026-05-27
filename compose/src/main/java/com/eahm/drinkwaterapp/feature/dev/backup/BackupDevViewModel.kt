package com.eahm.drinkwaterapp.feature.dev.backup

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.backup.BackupProviderRegistry
import com.eahm.drinkwaterapp.backup.BackupRepository
import com.eahm.drinkwaterapp.backup.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.CloudProviderType
import com.eahm.drinkwaterapp.backup.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.SyncBackupUseCase
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class BackupDevViewModel(
    private val application: Application,
    private val registry: BackupProviderRegistry,
    private val connectUseCase: ConnectBackupUseCase,
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val checkStatusUseCase: CheckBackupStatusUseCase,
    private val disconnectUseCase: DisconnectBackupUseCase,
    private val migrateUseCase: MigrateBackupUseCase,
    private val repository: BackupRepository,
) : ViewModel() {

    private val _events = Channel<BackupDevEvent>(Channel.BUFFERED)
    val events: Flow<BackupDevEvent> = _events.receiveAsFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())

    val state = combine(
        _log,
        repository.syncState,
    ) { log, syncState ->
        BackupDevState(syncState = syncState, log = log)
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
            BackupDevAction.ConnectDropbox -> connectProvider(CloudProviderType.DROPBOX)
            BackupDevAction.SyncDropbox -> syncProvider(CloudProviderType.DROPBOX)
            BackupDevAction.RestoreDropbox -> restoreProvider(CloudProviderType.DROPBOX)
            BackupDevAction.CheckStatusDropbox -> checkStatus(CloudProviderType.DROPBOX)
            BackupDevAction.DisconnectDropbox -> disconnectProvider(CloudProviderType.DROPBOX)
            BackupDevAction.MigrateGoogleToDropbox -> migrate(
                CloudProviderType.GOOGLE_DRIVE,
                CloudProviderType.DROPBOX,
            )

            BackupDevAction.MigrateDropboxToGoogle -> migrate(
                CloudProviderType.DROPBOX,
                CloudProviderType.GOOGLE_DRIVE,
            )

            BackupDevAction.ClearLog -> _log.value = emptyList()
        }
    }

    private fun connectProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Connect ${type.displayName}…")
        val result = connectUseCase(application, type)
        log("${type.displayName} connect → $result")
    }

    private fun syncProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Sync ${type.displayName}…")
        val result = syncUseCase(application, registry.get(type))
        log("${type.displayName} sync → $result")
    }

    private fun restoreProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Restore ${type.displayName}…")
        val result = restoreUseCase(application, registry.get(type))
        log("${type.displayName} restore → $result")
    }

    private fun checkStatus(type: CloudProviderType) = viewModelScope.launch {
        log("Check status ${type.displayName}…")
        val result = checkStatusUseCase(application, registry.get(type))
        log("${type.displayName} status → $result")
    }

    private fun disconnectProvider(type: CloudProviderType) = viewModelScope.launch {
        log("Disconnect ${type.displayName}…")
        registry.get(type).signOut(application)
        log("${type.displayName} signed out")
    }

    private fun migrate(from: CloudProviderType, to: CloudProviderType) = viewModelScope.launch {
        log("Migrate ${from.displayName} → ${to.displayName}…")
        val result = migrateUseCase(application, registry.get(from), registry.get(to))
        log("Migrate → $result")
    }

    private fun log(message: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val ts = "%02d:%02d:%02d".format(now.hour, now.minute, now.second)
        _log.update { it + "[$ts] $message" }
    }
}
