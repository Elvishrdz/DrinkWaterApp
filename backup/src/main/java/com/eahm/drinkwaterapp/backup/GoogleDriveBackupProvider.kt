package com.eahm.drinkwaterapp.backup

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
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

private const val TAG = "GoogleDriveBackup"
private const val APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
private const val EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email"
private const val PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile"
private const val BACKUP_FILE_NAME = "drinkwater_backup.json"
private const val DRIVE_BASE = "https://www.googleapis.com"
private const val USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
private const val REVOKE_URL = "https://oauth2.googleapis.com/revoke"

/**
 * Google Drive backup using the Google Identity Services **Authorization** API
 * ([Identity.getAuthorizationClient]) — the replacement for the deprecated
 * `GoogleSignIn` / `GoogleSignInOptions` flow.
 *
 * Unlike the old API, [AuthorizationResult.getAccessToken] returns a real OAuth
 * access token that can be used directly as a Bearer token against the Drive REST
 * API. Once the user grants consent, `authorize()` returns a fresh token silently
 * (no UI), which lets background sync mint tokens without an Activity.
 */
class GoogleDriveBackupProvider(
    private val httpClient: HttpClient,
    private val json: Json,
) : CloudBackupProvider {

    override val providerType: CloudProviderType = CloudProviderType.GOOGLE_DRIVE

    private val authorizationRequest: AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(Scope(APPDATA_SCOPE), Scope(EMAIL_SCOPE), Scope(PROFILE_SCOPE))
            )
            .build()

    /** Runs the authorization flow. Returns silently if consent was already granted. */
    private suspend fun authorize(context: Context): AuthorizationResult =
        suspendCancellableCoroutine { cont ->
            Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.cancel(it) }
        }

    /** Fresh OAuth access token, or null if user consent is still required. */
    private suspend fun getAccessToken(context: Context?): String? {
        val result = authorize(context!!)
        val grantedScopes = result.grantedScopes.map { it.toString() }
        Log.d(
            TAG,
            "authorize: hasResolution=${result.hasResolution()}, " +
                    "tokenPresent=${!result.accessToken.isNullOrBlank()}, " +
                    "grantedScopes=$grantedScopes",
        )
        if (result.hasResolution()) return null
        if (APPDATA_SCOPE !in grantedScopes) {
            Log.w(TAG, "Access token granted but missing $APPDATA_SCOPE scope")
            return null
        }
        return result.accessToken?.takeIf { it.isNotBlank() }
    }

    private suspend fun fetchUser(token: String): CloudUser? {
        val response = httpClient.get(USERINFO_URL) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val obj = runCatching {
            json.parseToJsonElement(response.bodyAsText()).jsonObject
        }.getOrNull()

        return obj?.let {
            val email = it["email"]?.jsonPrimitive?.content.orEmpty()
            val name = it["name"]?.jsonPrimitive?.content.orEmpty()

            CloudUser(displayName = name, email = email, providerType = providerType)
        }
    }

    /**
     * The legacy Intent-based entry point is unused for Google Drive — the
     * Authorization flow is asynchronous and returns an [IntentSender] for
     * consent. See [getAuthorizationIntentSender].
     */
    override fun getSignInIntent(context: Context?): Intent? = null

    /**
     * Returns an [IntentSender] to launch (via `StartIntentSenderForResult`) when
     * the user must grant consent, or null when access was already authorized.
     */
    suspend fun getAuthorizationIntentSender(context: Context?): IntentSender? {
        val result = authorize(context!!)
        return if (result.hasResolution()) result.pendingIntent?.intentSender else null
    }

    override suspend fun handleSignInResult(
        context: Context?,
        resultCode: Int,
        data: Intent?,
    ): BackupResult<CloudUser> = runCatching {
        val result = Identity.getAuthorizationClient(context!!)
            .getAuthorizationResultFromIntent(data)

        val token = result.accessToken
            ?: return BackupResult.Failure(BackupError.AuthFailed)

        val user = fetchUser(token)
            ?: return BackupResult.Failure(BackupError.AuthFailed)

        BackupResult.Success(user)

    }.getOrElse { BackupResult.Failure(BackupError.AuthFailed) }

    override suspend fun silentSignIn(context: Context?): BackupResult<CloudUser> = runCatching {
        val token = getAccessToken(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)

        val user = fetchUser(token)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)

        BackupResult.Success(user)
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun signOut(context: Context?): BackupResult<Unit> = runCatching {
        // Best-effort revoke so a later reconnect re-prompts for consent.
        val token = getAccessToken(context)
        if (token != null) {
            runCatching { httpClient.post(REVOKE_URL) { parameter("token", token) } }
        }
        BackupResult.Success(Unit)
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun isSignedIn(context: Context?): Boolean =
        runCatching { getAccessToken(context) != null }.getOrDefault(false)

    override suspend fun getCurrentUser(context: Context?): CloudUser? {
        val token = getAccessToken(context) ?: return null
        return runCatching { fetchUser(token) }.getOrNull()
    }

    private suspend fun findBackupFileId(token: String): String? {
        val response = httpClient.get("$DRIVE_BASE/drive/v3/files") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("spaces", "appDataFolder")
            parameter("fields", "files(id,name)")
            parameter("q", "name='$BACKUP_FILE_NAME'")
        }
        if (!response.status.isSuccess()) {
            Log.e(TAG, "findBackupFileId failed: ${response.status} -> ${response.bodyAsText()}")
            return null
        }
        val body = response.bodyAsText()
        val jsonObj = json.parseToJsonElement(body).jsonObject
        return jsonObj["files"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("id")?.jsonPrimitive?.content
    }

    override suspend fun upload(context: Context?, data: String): BackupResult<Unit> = runCatching {
        val token = getAccessToken(context)
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

        val requestBlock: HttpRequestBuilder.() -> Unit = {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.parse("multipart/related; boundary=$boundary"))
            setBody(multipartBody)
        }

        val response = if (existingId == null) {
            httpClient.post(url, requestBlock)
        } else {
            httpClient.patch(url, requestBlock)
        }

        if (response.status.isSuccess()) {
            BackupResult.Success(Unit)
        } else {
            Log.e(TAG, "upload failed: ${response.status} -> ${response.bodyAsText()}")
            BackupResult.Failure(BackupError.UploadFailed())
        }

    }.getOrElse { e ->
        if (e.message?.contains("quota", ignoreCase = true) == true)
            BackupResult.Failure(BackupError.QuotaExceeded())
        else
            BackupResult.Failure(BackupError.UploadFailed(e))
    }

    override suspend fun download(context: Context?): BackupResult<BackupFile> = runCatching {
        val token = getAccessToken(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)

        val fileId = findBackupFileId(token)
            ?: return BackupResult.Failure(BackupError.FileNotFound)

        val response = httpClient.get("$DRIVE_BASE/drive/v3/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("alt", "media")
        }

        if (!response.status.isSuccess()) {
            return BackupResult.Failure(BackupError.DownloadFailed())
        }

        val rawJson = runCatching {
            json.parseToJsonElement(response.bodyAsText()).jsonObject
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

    }.getOrElse { e -> BackupResult.Failure(BackupError.DownloadFailed(e)) }

    override suspend fun getRemoteMetadata(context: Context?): BackupResult<BackupMetadata?> =
        runCatching {
            val token = getAccessToken(context)
                ?: return BackupResult.Failure(BackupError.NotSignedIn)
            val fileId = findBackupFileId(token) ?: return BackupResult.Success(null)
            when (val result = download(context)) {
                is BackupResult.Success -> BackupResult.Success(result.value.metadata)
                is BackupResult.Failure -> result
            }
        }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }

    override suspend fun deleteRemoteFile(context: Context?): BackupResult<Unit> = runCatching {
        val token = getAccessToken(context)
            ?: return BackupResult.Failure(BackupError.NotSignedIn)
        val fileId = findBackupFileId(token) ?: return BackupResult.Success(Unit)
        val response = httpClient.delete("$DRIVE_BASE/drive/v3/files/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) BackupResult.Success(Unit)
        else BackupResult.Failure(BackupError.Unknown())
    }.getOrElse { e -> BackupResult.Failure(BackupError.Unknown(e)) }
}
