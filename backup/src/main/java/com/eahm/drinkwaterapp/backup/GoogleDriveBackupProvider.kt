package com.eahm.drinkwaterapp.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.resume

private const val APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
private const val BACKUP_FILE_NAME = "drinkwater_backup.json"
private const val DRIVE_BASE = "https://www.googleapis.com"

class GoogleDriveBackupProvider(
    private val httpClient: HttpClient,
    private val json: Json,
) : CloudBackupProvider {

    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE

    private fun buildSignInOptions() =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(APPDATA_SCOPE))
            .requestEmail()
            .build()

    override fun getSignInIntent(context: Context?): Intent? {
        val ctx = context!!
        val account = GoogleSignIn.getLastSignedInAccount(ctx)
        if (account != null && GoogleSignIn.hasPermissions(
                account,
                Scope(APPDATA_SCOPE)
            )
        ) return null
        return GoogleSignIn.getClient(ctx, buildSignInOptions()).signInIntent
    }

    override suspend fun handleSignInResult(
        context: Context?,
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser> = runCatching {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = suspendCancellableCoroutine<GoogleSignInAccount> { cont ->
            task.addOnSuccessListener { cont.resume(it) }
            task.addOnFailureListener { cont.cancel(it) }
        }
        BackupResult.Success(
            CloudUser(
                displayName = account.displayName ?: "",
                email = account.email ?: "",
                providerType = providerType,
            )
        )
    }.getOrElse { BackupResult.Failure(BackupError.AuthFailed) }

    override suspend fun silentSignIn(context: Context?): BackupResult<CloudUser> = runCatching {
        val ctx = context!!
        val account = GoogleSignIn.getLastSignedInAccount(ctx)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        if (!GoogleSignIn.hasPermissions(account, Scope(APPDATA_SCOPE))) {
            return BackupResult.Failure(BackupError.NotSignedIn)
        }
        BackupResult.Success(
            CloudUser(
                displayName = account.displayName ?: "",
                email = account.email ?: "",
                providerType = providerType,
            )
        )
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun signOut(context: Context?): BackupResult<Unit> = runCatching {
        val ctx = context!!
        val client = GoogleSignIn.getClient(ctx, buildSignInOptions())
        suspendCancellableCoroutine { cont ->
            client.signOut()
                .addOnSuccessListener { cont.resume(BackupResult.Success(Unit)) }
                .addOnFailureListener { cont.resume(BackupResult.Failure(BackupError.Unknown(it))) }
        }
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun isSignedIn(context: Context?): Boolean {
        val ctx = context!!
        val account = GoogleSignIn.getLastSignedInAccount(ctx) ?: return false
        return GoogleSignIn.hasPermissions(account, Scope(APPDATA_SCOPE))
    }

    override suspend fun getCurrentUser(context: Context?): CloudUser? {
        val ctx = context!!
        val account = GoogleSignIn.getLastSignedInAccount(ctx) ?: return null
        if (!GoogleSignIn.hasPermissions(account, Scope(APPDATA_SCOPE))) return null
        return CloudUser(
            displayName = account.displayName ?: "",
            email = account.email ?: "",
            providerType = providerType,
        )
    }

    private suspend fun getAccessToken(context: Context?): String? =
        GoogleSignIn.getLastSignedInAccount(context!!)?.serverAuthCode

    private suspend fun findBackupFileId(token: String): String? {
        val response = httpClient.get("$DRIVE_BASE/drive/v3/files") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("spaces", "appDataFolder")
            parameter("fields", "files(id,name)")
            parameter("q", "name='$BACKUP_FILE_NAME'")
        }
        if (!response.status.isSuccess()) return null
        val body = response.bodyAsText()
        val jsonObj = json.parseToJsonElement(body).jsonObject
        return jsonObj["files"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.content
    }

    override suspend fun upload(context: Context?, data: String): BackupResult<Unit> = runCatching {
        val token = GoogleSignIn.getLastSignedInAccount(context!!)?.serverAuthCode
            ?: return BackupResult.Failure(BackupError.NotSignedIn)

        val existingId = findBackupFileId(token)

        val metaPart = if (existingId == null) {
            """{"name":"$BACKUP_FILE_NAME","parents":["appDataFolder"]}"""
        } else {
            """{"name":"$BACKUP_FILE_NAME"}"""
        }

        val boundary = "backup_boundary_${System.currentTimeMillis()}"
        val multipartBody = "--$boundary\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n\r\n" +
                "$metaPart\r\n" +
                "--$boundary\r\n" +
                "Content-Type: application/json\r\n\r\n" +
                "$data\r\n" +
                "--$boundary--"

        val url = if (existingId == null) {
            "$DRIVE_BASE/upload/drive/v3/files?uploadType=multipart"
        } else {
            "$DRIVE_BASE/upload/drive/v3/files/$existingId?uploadType=multipart"
        }

        val response = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.parse("multipart/related; boundary=$boundary"))
            setBody(multipartBody)
        }
        if (response.status.isSuccess()) BackupResult.Success(Unit)
        else BackupResult.Failure(BackupError.UploadFailed())
    }.getOrElse { e ->
        if (e.message?.contains("quota", ignoreCase = true) == true)
            BackupResult.Failure(BackupError.QuotaExceeded())
        else BackupResult.Failure(BackupError.UploadFailed(e))
    }

    override suspend fun download(context: Context?): BackupResult<BackupFile> = runCatching {
        val token = GoogleSignIn.getLastSignedInAccount(context!!)?.serverAuthCode
            ?: return BackupResult.Failure(BackupError.NotSignedIn)

        val fileId = findBackupFileId(token)
            ?: return BackupResult.Failure(BackupError.FileNotFound)

        val response = httpClient.get("$DRIVE_BASE/drive/v3/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("alt", "media")
        }
        if (!response.status.isSuccess()) return BackupResult.Failure(BackupError.DownloadFailed())

        val rawJson = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val metadata =
            json.decodeFromJsonElement(BackupMetadata.serializer(), rawJson["metadata"]!!)
        BackupResult.Success(BackupFile(metadata = metadata, rawData = rawJson))
    }.getOrElse { e -> BackupResult.Failure(BackupError.DownloadFailed(e)) }

    override suspend fun getRemoteMetadata(context: Context?): BackupResult<BackupMetadata?> =
        runCatching {
            val token = GoogleSignIn.getLastSignedInAccount(context!!)?.serverAuthCode
                ?: return BackupResult.Failure(BackupError.NotSignedIn)
            val fileId = findBackupFileId(token) ?: return BackupResult.Success(null)
            when (val result = download(context)) {
                is BackupResult.Success -> BackupResult.Success(result.value.metadata)
                is BackupResult.Failure -> result
            }
        }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun deleteRemoteFile(context: Context?): BackupResult<Unit> = runCatching {
        val token = GoogleSignIn.getLastSignedInAccount(context!!)?.serverAuthCode
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val fileId = findBackupFileId(token) ?: return BackupResult.Success(Unit)
        val response = httpClient.delete("$DRIVE_BASE/drive/v3/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) BackupResult.Success(Unit)
        else BackupResult.Failure(BackupError.Unknown())
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }
}
