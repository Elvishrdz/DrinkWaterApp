package com.eahm.drinkwaterapp.backup.domain.model

data class TombstonePolicy(
    // Retention must exceed the longest realistic offline period: a device offline longer
    // than this, still holding a live copy of a deleted record, will resurrect it. See
    // docs/guide/tombstone-purge-guide.md §3.
    val retentionDays: Int = 60,
) {
    val retentionMs: Long get() = retentionDays * 24L * 60 * 60 * 1000
}
