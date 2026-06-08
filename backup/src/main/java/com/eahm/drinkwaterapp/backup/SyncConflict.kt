package com.eahm.drinkwaterapp.backup

sealed interface SyncConflict {

    data class GoalConflict(
        val effectiveDate: Int,
        val localGoal: GoalRecord,
        val remoteGoal: GoalRecord,
    ) : SyncConflict

    data class PreferencesConflict(
        val localPrefs: PreferencesRecord,
        val remotePrefs: PreferencesRecord,
    ) : SyncConflict
}
