package com.eahm.drinkwaterapp.backup.domain

import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import kotlinx.serialization.json.JsonObject

interface BackupDataSource {
    /**
     * Serializes the current local state into a backup envelope.
     *
     * @param createdAt the `created_at` to stamp into the file's metadata. Pass the existing
     * remote file's `created_at` to preserve it across updates; pass `null` when creating a
     * brand-new file (or replacing one) so the current time is used instead.
     */
    suspend fun serialize(createdAt: Long? = null): String
    suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit>

    /**
     * Returns the count of history/goal tombstone records whose deleted_at is between
     * [warningThreshold] and [expiryThreshold] (i.e. near expiry but not yet expired).
     */
    suspend fun findNearExpiryTombstones(warningThreshold: Long, expiryThreshold: Long): Int

    /**
     * Physically deletes all history and goal records where deleted_at <= [expiryThreshold].
     */
    suspend fun purgeExpiredTombstones(expiryThreshold: Long)

    /**
     * Reads the current local database and returns an in-memory [com.eahm.drinkwaterapp.backup.domain.model.BackupData] snapshot.
     * [deviceId] is stamped on any records that are missing a device identifier.
     */
    suspend fun readLocalData(deviceId: String): BackupData

    /**
     * Parses a remote backup [JsonObject] (already schema-migrated) into [BackupData].
     * Returns null if the JSON structure is unreadable.
     */
    suspend fun parseRemoteData(raw: JsonObject): BackupData?

    /**
     * Replaces all local history and goal records with the provided [BackupData].
     * Preferences are also applied. This is the merge-import path.
     */
    suspend fun writeBackupData(data: BackupData): BackupResult<Unit>

    /**
     * Order-independent fingerprint of the **local** data content (logs + goals +
     * preferences), excluding volatile metadata. Two databases with exactly the same
     * content produce the same fingerprint regardless of record ordering.
     */
    suspend fun localContentFingerprint(): String

    /**
     * Fingerprint of a **remote** backup's data content, computed identically to
     * [localContentFingerprint] so the two can be compared for exact equality.
     * [raw] is the full remote envelope (already schema-migrated).
     */
    fun remoteContentFingerprint(raw: JsonObject): String
}
