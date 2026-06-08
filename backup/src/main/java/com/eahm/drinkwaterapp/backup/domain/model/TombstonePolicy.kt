package com.eahm.drinkwaterapp.backup.domain.model

data class TombstonePolicy(
    val retentionDays: Int = 15,
) {
    val retentionMs: Long get() = retentionDays * 24L * 60 * 60 * 1000
}
