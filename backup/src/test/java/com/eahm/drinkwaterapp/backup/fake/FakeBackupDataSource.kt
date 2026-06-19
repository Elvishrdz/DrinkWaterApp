package com.eahm.drinkwaterapp.backup.fake

import com.eahm.drinkwaterapp.backup.domain.BackupDataSource
import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.BackupResult
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import kotlinx.serialization.json.JsonObject

class FakeBackupDataSource(
    private val serialized: String = "{}",
    private val importResult: BackupResult<Unit> = BackupResult.Success(Unit),
) : BackupDataSource {
    var serializeCallCount = 0
    var lastImported: JsonObject? = null

    override suspend fun serialize(createdAt: Long?): String {
        serializeCallCount++
        return serialized
    }

    override suspend fun deserializeAndImport(data: JsonObject): BackupResult<Unit> {
        lastImported = data
        return importResult
    }

    override suspend fun findNearExpiryTombstones(
        warningThreshold: Long,
        expiryThreshold: Long
    ): Int = 0

    override suspend fun purgeExpiredTombstones(expiryThreshold: Long) {}

    override suspend fun readLocalData(deviceId: String): BackupData =
        BackupData(
            history = emptyList(),
            dailyGoals = emptyList(),
            preferences = PreferencesRecord("ML", 250f),
        )

    override suspend fun parseRemoteData(raw: JsonObject): BackupData? = null

    override suspend fun writeBackupData(data: BackupData): BackupResult<Unit> =
        BackupResult.Success(Unit)

    // Distinct by default so the initial-connection flow treats local/remote as different.
    var localFingerprint: String = "local-fingerprint"
    var remoteFingerprint: String = "remote-fingerprint"

    override suspend fun localContentFingerprint(): String = localFingerprint

    override fun remoteContentFingerprint(raw: JsonObject): String = remoteFingerprint
}
