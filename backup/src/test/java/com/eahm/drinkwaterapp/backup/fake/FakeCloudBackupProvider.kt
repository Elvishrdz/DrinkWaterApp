package com.eahm.drinkwaterapp.backup.fake

import android.content.Intent
import com.eahm.drinkwaterapp.backup.domain.CloudBackupProvider
import com.eahm.drinkwaterapp.backup.domain.model.BackupError
import com.eahm.drinkwaterapp.backup.domain.model.BackupFile
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser

class FakeCloudBackupProvider(
    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE,
    private val signInResult: BackupResult<CloudUser> = BackupResult.Success(
        CloudUser("Test User", providerType = CloudProviderType.GOOGLE_DRIVE)
    ),
    private val uploadResult: BackupResult<Unit> = BackupResult.Success(Unit),
    private val downloadResult: BackupResult<BackupFile> = BackupResult.Failure(BackupError.FileNotFound),
    private val metadataResult: BackupResult<BackupMetadata?> = BackupResult.Success(null),
) : CloudBackupProvider {

    var uploadCallCount = 0
    var downloadCallCount = 0
    var signOutCalled = false
    var lastUploadedData: String? = null

    override fun getSignInIntent(): Intent? = null

    override suspend fun handleSignInResult(
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser> = signInResult

    override suspend fun silentSignIn(): BackupResult<CloudUser> = signInResult

    override suspend fun signOut(): BackupResult<Unit> {
        signOutCalled = true
        return BackupResult.Success(Unit)
    }

    override suspend fun isSignedIn(): Boolean =
        signInResult is BackupResult.Success

    override suspend fun getCurrentUser(): CloudUser? =
        (signInResult as? BackupResult.Success)?.value

    override suspend fun upload(data: String): BackupResult<Unit> {
        uploadCallCount++
        lastUploadedData = data
        return uploadResult
    }

    override suspend fun download(): BackupResult<BackupFile> {
        downloadCallCount++
        return downloadResult
    }

    override suspend fun getRemoteMetadata(): BackupResult<BackupMetadata?> =
        metadataResult

    override suspend fun deleteRemoteFile(): BackupResult<Unit> =
        BackupResult.Success(Unit)
}
