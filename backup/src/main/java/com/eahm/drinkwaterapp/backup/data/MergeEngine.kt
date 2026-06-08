package com.eahm.drinkwaterapp.backup.data

import com.eahm.drinkwaterapp.backup.domain.model.BackupData
import com.eahm.drinkwaterapp.backup.domain.model.MergeResult
import com.eahm.drinkwaterapp.backup.domain.model.PreferencesRecord
import com.eahm.drinkwaterapp.backup.domain.model.SyncConflict
import kotlin.math.abs

class MergeEngine {

    companion object {
        private const val GOAL_CONFLICT_WINDOW_MS = 60_000L
        private const val PREFS_CONFLICT_WINDOW_MS = 300_000L
    }

    fun merge(
        local: BackupData,
        remote: BackupData,
        localDeviceId: String,
    ): MergeResult {
        var appliedRemoteRecords = 0
        var autoResolvedConflicts = 0
        var tombstonesApplied = 0
        val conflicts = mutableListOf<SyncConflict>()

        // --- Merge history ---
        val localById = local.history.associateBy { it.id }.toMutableMap()
        val remoteById = remote.history.associateBy { it.id }

        for ((id, remoteRecord) in remoteById) {
            val localRecord = localById[id]
            if (localRecord == null) {
                // New record from remote
                localById[id] = remoteRecord
                if (remoteRecord.deletedAt == null) appliedRemoteRecords++
                else tombstonesApplied++ // tombstone for a record we never had — just store it
            } else {
                // Both sides have it
                if (remoteRecord.updatedAt > localRecord.updatedAt) {
                    localById[id] = remoteRecord
                    appliedRemoteRecords++
                    if (remoteRecord.deletedAt != null && localRecord.deletedAt == null) {
                        tombstonesApplied++
                    }
                }
                // else: local is newer or equal, keep local
            }
        }
        val mergedHistory = localById.values.toList()

        // --- Merge goals ---
        val localByDate = local.dailyGoals.associateBy { it.effectiveDate }.toMutableMap()
        val remoteByDate = remote.dailyGoals.associateBy { it.effectiveDate }

        for ((date, remoteGoal) in remoteByDate) {
            val localGoal = localByDate[date]
            if (localGoal == null) {
                localByDate[date] = remoteGoal
                appliedRemoteRecords++
            } else if (localGoal.goalMl != remoteGoal.goalMl || localGoal.deletedAt != remoteGoal.deletedAt) {
                val delta = abs(localGoal.updatedAt - remoteGoal.updatedAt)
                if (delta < GOAL_CONFLICT_WINDOW_MS) {
                    conflicts.add(SyncConflict.GoalConflict(date, localGoal, remoteGoal))
                    // Leave local in place for now; ViewModel resolves and re-applies
                } else if (remoteGoal.updatedAt > localGoal.updatedAt) {
                    localByDate[date] = remoteGoal
                    autoResolvedConflicts++
                    appliedRemoteRecords++
                }
            }
            // else: same goalMl and deletedAt — no-op
        }
        val mergedGoals = localByDate.values.toList()

        // --- Merge preferences ---
        val mergedPrefs: PreferencesRecord
        val localPrefs = local.preferences
        val remotePrefs = remote.preferences

        val prefsDelta = abs(localPrefs.updatedAt - remotePrefs.updatedAt)
        val prefsAreDifferent = localPrefs.unitPreference != remotePrefs.unitPreference ||
                localPrefs.timeFormat != remotePrefs.timeFormat ||
                localPrefs.amountPerDrink != remotePrefs.amountPerDrink

        if (localPrefs.updatedAt >= remotePrefs.updatedAt) {
            mergedPrefs = localPrefs
        } else {
            mergedPrefs = remotePrefs
            appliedRemoteRecords++
            if (prefsDelta < PREFS_CONFLICT_WINDOW_MS && prefsAreDifferent) {
                conflicts.add(SyncConflict.PreferencesConflict(localPrefs, remotePrefs))
            } else if (prefsAreDifferent) {
                autoResolvedConflicts++
            }
        }

        return MergeResult(
            mergedData = BackupData(mergedHistory, mergedGoals, mergedPrefs),
            appliedRemoteRecords = appliedRemoteRecords,
            autoResolvedConflicts = autoResolvedConflicts,
            tombstonesApplied = tombstonesApplied,
            conflicts = conflicts,
        )
    }

    /** Apply a user's conflict resolution for a goal conflict, updating the merged goals list. */
    fun resolveGoalConflict(
        current: BackupData,
        conflict: SyncConflict.GoalConflict,
        useRemote: Boolean,
    ): BackupData {
        val winner = if (useRemote) conflict.remoteGoal else conflict.localGoal
        val updatedGoals = current.dailyGoals.map { goal ->
            if (goal.effectiveDate == conflict.effectiveDate) winner else goal
        }
        return current.copy(dailyGoals = updatedGoals)
    }

    /** Apply a user's conflict resolution for a preferences conflict. */
    fun resolvePrefsConflict(
        current: BackupData,
        conflict: SyncConflict.PreferencesConflict,
        useRemote: Boolean,
    ): BackupData {
        val winner = if (useRemote) conflict.remotePrefs else conflict.localPrefs
        return current.copy(preferences = winner)
    }
}