package com.eahm.drinkwaterapp.backup.domain.model

/** What kicked off a sync — used to phrase the status/error message shown in Settings. */
enum class SyncTrigger {
    MANUAL,
    ON_SAVE,
    PERIODIC,
    FOREGROUND,
    RECONNECT,
    CONNECT,
}

/**
 * After a failed sync, what happens next. Drives whether the UI reassures the user a retry is
 * coming, or tells them to retry manually.
 */
enum class SyncDisposition {
    /** Offline / network constraint unmet — will flush automatically when connectivity returns. */
    WAITING_FOR_CONNECTION,

    /** Retryable error with attempts remaining — WorkManager will retry with backoff. */
    WILL_RETRY,

    /** Non-retryable, or retries exhausted — no automatic retry; prompt manual "Sync now". */
    FAILED,
}

/**
 * Single source of truth for backup sync progress. Written by every sync trigger (manual,
 * background worker, connect) and observed by the Settings UI.
 */
data class BackupSyncStatus(
    val running: Boolean = false,
    val activeTrigger: SyncTrigger? = null,
    val lastOutcome: Outcome? = null,
) {
    sealed interface Outcome {
        data class Success(val syncedAt: Long, val trigger: SyncTrigger) : Outcome
        data class Failure(
            val error: BackupError,
            val trigger: SyncTrigger,
            val disposition: SyncDisposition,
        ) : Outcome
    }
}
