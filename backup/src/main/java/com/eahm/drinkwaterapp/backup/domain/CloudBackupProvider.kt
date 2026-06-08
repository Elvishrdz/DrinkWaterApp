package com.eahm.drinkwaterapp.backup.domain

import android.content.Intent
import com.eahm.drinkwaterapp.backup.domain.model.BackupFile
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser

interface CloudBackupProvider {
    val providerType: CloudProviderType

    /** Returns an Intent to launch for sign-in, or null if already authorized. */
    fun getSignInIntent(): Intent?

    /** Called with the Activity result to finalize sign-in. */
    suspend fun handleSignInResult(
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser>

    /** Attempt sign-in silently (no UI) using cached credentials. */
    suspend fun silentSignIn(): BackupResult<CloudUser>

    suspend fun signOut(): BackupResult<Unit>

    suspend fun isSignedIn(): Boolean

    suspend fun getCurrentUser(): CloudUser?

    suspend fun upload(data: String): BackupResult<Unit>

    suspend fun download(): BackupResult<BackupFile>

    suspend fun getRemoteMetadata(): BackupResult<BackupMetadata?>

    suspend fun deleteRemoteFile(): BackupResult<Unit>
}
