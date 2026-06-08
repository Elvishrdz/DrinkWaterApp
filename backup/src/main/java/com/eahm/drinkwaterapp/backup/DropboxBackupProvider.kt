package com.eahm.drinkwaterapp.backup

import android.content.Context
import android.content.Intent
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val BACKUP_PATH = "/drinkwater_backup.json"
private const val CLIENT_IDENTIFIER = "DrinkWaterApp"

class DropboxBackupProvider(
    private val json: Json,
) : CloudBackupProvider {

    override val providerType: CloudProviderType = CloudProviderType.DROPBOX

    private val requestConfig: DbxRequestConfig by lazy {
        DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER).build()
    }

    private fun getStoredCredential(context: Context?): DbxCredential? {
        val prefs = context!!.getSharedPreferences("dropbox_prefs", Context.MODE_PRIVATE)
        val serialized = prefs.getString("credential", null) ?: return null
        return runCatching { DbxCredential.Reader.readFully(serialized) }.getOrNull()
    }

    private fun storeCredential(context: Context?, credential: DbxCredential) {
        val prefs = context!!.getSharedPreferences("dropbox_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("credential", credential.toString()).apply()
    }

    private fun clearCredential(context: Context?) {
        val prefs = context!!.getSharedPreferences("dropbox_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("credential").apply()
    }

    private fun buildClient(credential: DbxCredential): DbxClientV2 =
        DbxClientV2(requestConfig, credential)

    override fun getSignInIntent(context: Context?): Intent? {
        if (getStoredCredential(context) != null) return null
        // Dropbox uses Auth.startOAuth2PKCE which starts an Activity internally.
        // Return a sentinel intent to signal the caller should invoke startOAuth.
        return Intent().also { it.putExtra("dropbox_oauth", true) }
    }

    /** Launches the Dropbox PKCE OAuth flow. Must be called from an Activity context. */
    fun startOAuth(context: Context?) {
        Auth.startOAuth2PKCE(context!!, BuildConfig.DROPBOX_APP_KEY, requestConfig)
    }

    /** Call Auth.startOAuth2PKCE(context, appKey, requestConfig) from the Activity before this. */
    override suspend fun handleSignInResult(
        context: Context?,
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser> = runCatching {
        val credential = Auth.getDbxCredential()
            ?: return BackupResult.Failure(BackupError.AuthCancelled)
        storeCredential(context, credential)
        val client = buildClient(credential)
        val account = client.users().currentAccount
        BackupResult.Success(
            CloudUser(
                displayName = account.name.displayName,
                email = account.email,
                providerType = providerType,
            )
        )
    }.getOrElse { BackupResult.Failure(BackupError.AuthFailed) }

    override suspend fun silentSignIn(context: Context?): BackupResult<CloudUser> = runCatching {
        val credential = getStoredCredential(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val client = buildClient(credential)
        val account = client.users().currentAccount
        BackupResult.Success(
            CloudUser(
                displayName = account.name.displayName,
                email = account.email,
                providerType = providerType,
            )
        )
    }.getOrElse { BackupResult.Failure(BackupError.NotSignedIn) }

    override suspend fun signOut(context: Context?): BackupResult<Unit> = runCatching {
        clearCredential(context)
        BackupResult.Success(Unit)
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun isSignedIn(context: Context?): Boolean =
        getStoredCredential(context) != null

    override suspend fun getCurrentUser(context: Context?): CloudUser? = runCatching {
        val credential = getStoredCredential(context) ?: return null
        val client = buildClient(credential)
        val account = client.users().currentAccount
        CloudUser(
            displayName = account.name.displayName,
            email = account.email,
            providerType = providerType,
        )
    }.getOrNull()

    override suspend fun upload(context: Context?, data: String): BackupResult<Unit> = runCatching {
        val credential = getStoredCredential(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val client = buildClient(credential)
        val bytes = data.toByteArray(Charsets.UTF_8)
        ByteArrayInputStream(bytes).use { stream ->
            client.files()
                .uploadBuilder(BACKUP_PATH)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(stream)
        }
        BackupResult.Success(Unit)
    }.getOrElse { e ->
        val msg = e.message ?: ""
        if (msg.contains("quota", ignoreCase = true) || msg.contains("storage", ignoreCase = true))
            BackupResult.Failure(BackupError.QuotaExceeded())
        else BackupResult.Failure(BackupError.UploadFailed(e))
    }

    override suspend fun download(context: Context?): BackupResult<BackupFile> = runCatching {
        val credential = getStoredCredential(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val client = buildClient(credential)
        val outputStream = ByteArrayOutputStream()
        client.files().downloadBuilder(BACKUP_PATH).download(outputStream)
        val rawString = outputStream.toString(Charsets.UTF_8.name())

        val rawJson = runCatching {
            json.parseToJsonElement(rawString).jsonObject
        }.getOrNull()
            ?: return BackupResult.Failure(BackupError.RemoteCorrupted)

        val metadataElement = rawJson["metadata"]
            ?: return BackupResult.Failure(BackupError.RemoteCorrupted)

        val metadata = runCatching {
            json.decodeFromJsonElement(
                deserializer = BackupMetadata.serializer(),
                element = metadataElement,
            )
        }.getOrNull()
            ?: return BackupResult.Failure(BackupError.RemoteCorrupted)

        BackupResult.Success(BackupFile(metadata = metadata, rawData = rawJson))

    }.getOrElse { e ->
        if (e.message?.contains("not_found", ignoreCase = true) == true)
            BackupResult.Failure(BackupError.FileNotFound)
        else
            BackupResult.Failure(BackupError.DownloadFailed(e))
    }

    override suspend fun getRemoteMetadata(context: Context?): BackupResult<BackupMetadata?> =
        runCatching {
            val credential = getStoredCredential(context)
                ?: return BackupResult.Failure(BackupError.NotSignedIn)
            when (val result = download(context)) {
                is BackupResult.Success -> BackupResult.Success(result.value.metadata)
                is BackupResult.Failure -> {
                    if (result.error is BackupError.FileNotFound) BackupResult.Success(null)
                    else result
                }
            }
        }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun deleteRemoteFile(context: Context?): BackupResult<Unit> = runCatching {
        val credential = getStoredCredential(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val client = buildClient(credential)
        client.files().deleteV2(BACKUP_PATH)
        BackupResult.Success(Unit)
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }
}
