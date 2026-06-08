package com.eahm.drinkwaterapp.backup.domain.model

/**
 * Describes a remote backup that exists but does not match our specification, so it
 * cannot be merged. Presented to the user so they can decide whether to overwrite it
 * with the current device's data (a fresh backup) or leave the remote file untouched.
 */
data class CorruptedBackup(
    val reason: Reason,
) {
    enum class Reason {
        /** The file could not be read at all (invalid JSON, missing/garbled metadata). */
        UNREADABLE,

        /** The file was readable but its data structure is not recognizable. */
        UNRECOGNIZED_STRUCTURE,
    }
}
