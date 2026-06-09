package com.eahm.drinkwaterapp.backup.domain.model

/**
 * The user's choice when connecting to a provider that already has a backup file.
 * Presented only on first connection when a remote file exists, so the user controls
 * the (destructive) direction instead of an automatic merge.
 */
enum class InitialSyncDecision {
    /**
     * Merge local and remote data using the normal sync logic, resolving any conflicts.
     * Non-destructive: nothing is written locally or remotely until every conflict is
     * resolved, so abandoning the process leaves both sides untouched.
     */
    MERGE,

    /** Download the remote backup and overwrite this device's local data. */
    USE_REMOTE,

    /** Upload this device's data and overwrite the remote backup. */
    UPLOAD_LOCAL,
}
