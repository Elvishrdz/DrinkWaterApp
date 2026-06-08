package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.data.usecase.CheckBackupStatusUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.ConnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.DisconnectBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.MigrateBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.RestoreBackupUseCase
import com.eahm.drinkwaterapp.backup.data.usecase.SyncBackupUseCase
import com.eahm.drinkwaterapp.backup.domain.BackupPreferences
import com.eahm.drinkwaterapp.backup.domain.BackupRepository
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser
import com.eahm.drinkwaterapp.backup.domain.model.RemoteBackupStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class BackupRepositoryImpl(
    private val registry: BackupProviderRegistry,
    private val prefs: BackupPreferences,
    private val connectUseCase: ConnectBackupUseCase,
    private val syncUseCase: SyncBackupUseCase,
    private val restoreUseCase: RestoreBackupUseCase,
    private val checkStatusUseCase: CheckBackupStatusUseCase,
    private val disconnectUseCase: DisconnectBackupUseCase,
    private val migrateUseCase: MigrateBackupUseCase,
) : BackupRepository {

    private val _syncState = MutableStateFlow<BackupSyncState>(BackupSyncState.Idle)
    override val syncState: StateFlow<BackupSyncState> = _syncState.asStateFlow()

    private suspend fun resolveProvider(): BackupResult<CloudBackupProvider> {
        val providerId = prefs.getBackupProvider()
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        val type = CloudProviderType.fromId(providerId)
            ?: return BackupResult.Failure(BackupError.NoProviderSelected)
        return BackupResult.Success(registry.get(type))
    }

    override suspend fun connect(
        providerType: CloudProviderType,
    ): BackupResult<CloudUser> = withContext(Dispatchers.IO) {
        _syncState.value = BackupSyncState.InProgress
        connectUseCase(providerType).also { result ->
            _syncState.value = when (result) {
                is BackupResult.Success -> BackupSyncState.Success(System.currentTimeMillis())
                is BackupResult.Failure -> BackupSyncState.Error(result.error)
            }
        }
    }

    override suspend fun sync(): BackupResult<SyncResult> = withContext(Dispatchers.IO) {
        if (_syncState.value is BackupSyncState.InProgress) {
            return@withContext BackupResult.Success(SyncResult.AlreadyInProgress)
        }
        _syncState.value = BackupSyncState.InProgress
        val provider = resolveProvider()
        if (provider is BackupResult.Failure) {
            _syncState.value = BackupSyncState.Error(provider.error)
            return@withContext provider
        }
        syncUseCase(
            provider = (provider as BackupResult.Success).value,
        ).also { result ->
            _syncState.value = when (result) {
                is BackupResult.Success -> BackupSyncState.Success(syncedAt = System.currentTimeMillis())
                is BackupResult.Failure -> BackupSyncState.Error(error = result.error)
            }
        }
    }

    override suspend fun restore(): BackupResult<Unit> =
        withContext(Dispatchers.IO) {
            val provider = resolveProvider()
            if (provider is BackupResult.Failure) return@withContext provider
            restoreUseCase((provider as BackupResult.Success).value)
        }

    override suspend fun checkRemoteStatus(): BackupResult<RemoteBackupStatus?> =
        withContext(Dispatchers.IO) {
            val provider = resolveProvider()
            if (provider is BackupResult.Failure) return@withContext provider
            checkStatusUseCase((provider as BackupResult.Success).value)
        }

    override suspend fun disconnect(): BackupResult<Unit> =
        withContext(Dispatchers.IO) { disconnectUseCase() }

    override suspend fun migrateProvider(
        from: CloudProviderType,
        to: CloudProviderType,
    ): BackupResult<Unit> = withContext(Dispatchers.IO) {
        val fromProvider = registry.get(from)
        val toProvider = registry.get(to)
        migrateUseCase(fromProvider, toProvider)
    }
}
