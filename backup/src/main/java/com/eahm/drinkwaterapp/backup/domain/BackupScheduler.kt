package com.eahm.drinkwaterapp.backup.domain

interface BackupScheduler {
    /**
     * Schedule (or reschedule) the daily auto-backup to run at [hourOfDay] (0–23). Implemented as
     * a self-rescheduling one-time work request, since WorkManager periodic work cannot target a
     * clock time. The worker re-arms the next day on completion.
     */
    fun scheduleDailyAutoSync(hourOfDay: Int)
    fun triggerManualSync()
    fun cancelAll()

    /** Enqueue a debounced one-time sync triggered by a data write. Uses KEEP policy — ignored if one is already queued. */
    fun triggerOnChangeSync()

    /** Enqueue a one-time foreground sync (app came to foreground). Uses KEEP policy. */
    fun triggerForegroundSync()
}
