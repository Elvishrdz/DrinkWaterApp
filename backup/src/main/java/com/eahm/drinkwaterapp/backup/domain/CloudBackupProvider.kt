package com.eahm.drinkwaterapp.backup.domain

import android.content.Context
import android.content.Intent
import com.eahm.drinkwaterapp.backup.domain.model.BackupFile
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.CloudUser

// TODO: remove the context parameter and get it internally in the implementation to avoid passing null
interface CloudBackupProvider {
    val providerType: CloudProviderType

    /** Returns an Intent to launch for sign-in, or null if already authorized. */
    fun getSignInIntent(context: Context?): Intent?

    /** Called with the Activity result to finalize sign-in. */
    suspend fun handleSignInResult(
        context: Context?,
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser>

    /** Attempt sign-in silently (no UI) using cached credentials. */
    suspend fun silentSignIn(context: Context?): BackupResult<CloudUser>

    suspend fun signOut(context: Context?): BackupResult<Unit>

    suspend fun isSignedIn(context: Context?): Boolean

    suspend fun getCurrentUser(context: Context?): CloudUser?

    suspend fun upload(context: Context?, data: String): BackupResult<Unit>

    suspend fun download(context: Context?): BackupResult<BackupFile>

    suspend fun getRemoteMetadata(context: Context?): BackupResult<BackupMetadata?>

    suspend fun deleteRemoteFile(context: Context?): BackupResult<Unit>
}
