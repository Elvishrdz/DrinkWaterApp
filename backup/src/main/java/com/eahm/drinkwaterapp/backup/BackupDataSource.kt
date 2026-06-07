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
}
