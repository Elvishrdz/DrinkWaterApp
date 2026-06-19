package com.eahm.drinkwaterapp.backup.fake

import android.content.Intent
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupFile
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * In-memory cloud: [upload] stores the serialized string and [download] returns it parsed back into
 * a [BackupFile], so an upload→download round-trip works (unlike [FakeCloudBackupProvider], which
 * can't). Use this for end-to-end sync scenarios.
 */
class InMemoryCloudBackupProvider(
    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE,
    /** Seed an already-existing remote backup (as a serialized envelope), or null for "no file". */
    seedRemote: String? = null,
) : CloudBackupProvider {

    private var stored: String? = seedRemote
    var uploadCallCount = 0
    var downloadCallCount = 0
    var lastUploadedData: String? = null

    override fun getSignInIntent(): Intent? = null
    override suspend fun handleSignInResult(
        resultCode: Int,
        data: Intent?
    ): BackupResult<CloudUser> =
        BackupResult.Success(CloudUser("Test User", providerType = providerType))

    override suspend fun silentSignIn(): BackupResult<CloudUser> =
        BackupResult.Success(CloudUser("Test User", providerType = providerType))

    override suspend fun signOut(): BackupResult<Unit> = BackupResult.Success(Unit)
    override suspend fun isSignedIn(): Boolean = true
    override suspend fun getCurrentUser(): CloudUser =
        CloudUser("Test User", providerType = providerType)

    override suspend fun upload(data: String): BackupResult<Unit> {
        uploadCallCount++
        lastUploadedData = data
        stored = data
        return BackupResult.Success(Unit)
    }

    override suspend fun download(): BackupResult<BackupFile> {
        downloadCallCount++
        val raw = stored ?: return BackupResult.Failure(BackupError.FileNotFound)
        val root = Json.parseToJsonElement(raw).jsonObject
        val meta = root["metadata"]?.jsonObject
        val metadata = BackupMetadata(
            schemaVersion = meta?.get("schema_version")?.jsonPrimitive?.int ?: 1,
            createdAt = meta?.get("created_at")?.jsonPrimitive?.longOrNull ?: 0L,
            lastUpdatedAt = meta?.get("last_updated_at")?.jsonPrimitive?.longOrNull ?: 0L,
        )
        return BackupResult.Success(BackupFile(metadata = metadata, rawData = root))
    }

    override suspend fun getRemoteMetadata(): BackupResult<BackupMetadata?> =
        BackupResult.Success(null)

    override suspend fun deleteRemoteFile(): BackupResult<Unit> {
        stored = null
        return BackupResult.Success(Unit)
    }

    /** Current remote envelope as a parsed JsonObject, for assertions; null when no file. */
    fun storedEnvelope(): JsonObject? = stored?.let { Json.parseToJsonElement(it).jsonObject }
}
