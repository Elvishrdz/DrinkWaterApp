package com.eahm.drinkwaterapp.backup.domain

import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser
import com.eahm.drinkwaterapp.backup.domain.model.RemoteBackupStatus
import com.eahm.drinkwaterapp.backup.domain.model.SyncResult
import kotlinx.coroutines.flow.StateFlow

// TODO: remove the context parameter and get it internally in the implementation to avoid passing null
interface BackupRepository {
    val syncState: StateFlow<BackupSyncState>

    suspend fun connect(providerType: CloudProviderType): BackupResult<CloudUser>
    suspend fun sync(): BackupResult<SyncResult>
    suspend fun restore(): BackupResult<Unit>
    suspend fun checkRemoteStatus(): BackupResult<RemoteBackupStatus?>
    suspend fun disconnect(): BackupResult<Unit>
    suspend fun migrateProvider(
        from: CloudProviderType,
        to: CloudProviderType,
    ): BackupResult<Unit>
}
