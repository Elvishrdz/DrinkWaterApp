package com.eahm.drinkwaterapp.backup.fake

import android.content.Context
import android.content.Intent
import com.eahm.drinkwaterapp.backup.BackupFile
import com.eahm.drinkwaterapp.backup.BackupMetadata
import com.eahm.drinkwaterapp.backup.BackupResult
import com.eahm.drinkwaterapp.backup.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.CloudProviderType
import com.eahm.drinkwaterapp.backup.CloudUser
import kotlinx.serialization.json.buildJsonObject

class FakeCloudBackupProvider(
    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE,
    private val signInResult: BackupResult<CloudUser> = BackupResult.Success(
        CloudUser("Test User", providerType = CloudProviderType.GOOGLE_DRIVE)
    ),
    private val uploadResult: BackupResult<Unit> = BackupResult.Success(Unit),
    private val downloadResult: BackupResult<BackupFile> = BackupResult.Success(
        BackupFile(
            metadata = BackupMetadata(
                schemaVersion = 1,
                appVersion = "1.0",
                createdAt = 0L,
                deviceId = "test-device",
            ),
            rawData = buildJsonObject {},
        )
    ),
    private val metadataResult: BackupResult<BackupMetadata?> = BackupResult.Success(null),
) : CloudBackupProvider {

    var uploadCallCount = 0
    var downloadCallCount = 0
    var signOutCalled = false
    var lastUploadedData: String? = null

    override fun getSignInIntent(context: Context?): Intent? = null

    override suspend fun handleSignInResult(
        context: Context?,
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser> = signInResult

    override suspend fun silentSignIn(context: Context?): BackupResult<CloudUser> = signInResult

    override suspend fun signOut(context: Context?): BackupResult<Unit> {
        signOutCalled = true
        return BackupResult.Success(Unit)
    }

    override suspend fun isSignedIn(context: Context?): Boolean =
        signInResult is BackupResult.Success

    override suspend fun getCurrentUser(context: Context?): CloudUser? =
        (signInResult as? BackupResult.Success)?.value

    override suspend fun upload(context: Context?, data: String): BackupResult<Unit> {
        uploadCallCount++
        lastUploadedData = data
        return uploadResult
    }

    override suspend fun download(context: Context?): BackupResult<BackupFile> {
        downloadCallCount++
        return downloadResult
    }

    override suspend fun getRemoteMetadata(context: Context?): BackupResult<BackupMetadata?> =
        metadataResult

    override suspend fun deleteRemoteFile(context: Context?): BackupResult<Unit> =
        BackupResult.Success(Unit)
}
