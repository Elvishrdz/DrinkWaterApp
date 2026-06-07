package com.eahm.drinkwaterapp.backup

import kotlinx.serialization.json.JsonObject

interface BackupDataSource {
    suspend fun serialize(): String
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
     * Reads the current local database and returns an in-memory [BackupData] snapshot.
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
}
