package com.eahm.drinkwaterapp.backup.domain

import com.eahm.drinkwaterapp.backup.domain.model.BackupSchedule

interface BackupScheduler {
    /**
     * Schedule (or reschedule) the automatic background backup per [schedule] — either a fixed
     * daily hour or a recurring interval. Implemented as a self-rescheduling one-time work request
     * (WorkManager periodic work can't target a clock time), so the worker re-arms the next run on
     * completion using the latest persisted schedule.
     */
    fun scheduleAutoSync(schedule: BackupSchedule)
    fun triggerManualSync()
    fun cancelAll()

    /** Enqueue a debounced one-time sync triggered by a data write. Uses KEEP policy — ignored if one is already queued. */
    fun triggerOnChangeSync()

    /**
     * Enqueue a single follow-up on-change sync from *within* a running sync worker, when changes
     * landed mid-run. Uses APPEND_OR_REPLACE so it queues behind the current worker (which is still
     * RUNNING) instead of being dropped. The worker calls this at most once per run, so the queue
     * stays bounded to one running + one queued.
     */
    fun enqueueFollowUpOnChangeSync()

    /** Enqueue a one-time foreground sync (app came to foreground). Uses KEEP policy. */
    fun triggerForegroundSync()
}
