package com.eahm.drinkwaterapp.backup.data

import java.util.concurrent.atomic.AtomicLong

/**
 * Process-wide monotonic counter of local data changes, used to detect whether new writes landed
 * *during* an in-flight sync.
 *
 * The sync worker captures [snapshot] before it runs; if [hasChangedSince] is true afterwards, a
 * write happened mid-sync that the just-finished run may not have uploaded, so the worker enqueues
 * exactly one follow-up. This keeps the on-change work bounded to "one running + one queued" while
 * still guaranteeing the latest write is eventually backed up.
 */
class BackupChangeTracker {
    private val revision = AtomicLong(0)

    /** Record a local data change. Safe to call from any thread. */
    fun markChanged() {
        revision.incrementAndGet()
    }

    /** Current revision, to be compared later via [hasChangedSince]. */
    fun snapshot(): Long = revision.get()

    /** True if any change was recorded since [snapshot] was taken. */
    fun hasChangedSince(snapshot: Long): Boolean = revision.get() != snapshot
}
