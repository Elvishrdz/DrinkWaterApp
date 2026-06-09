package com.eahm.drinkwaterapp.backup.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.security.MessageDigest

/**
 * Computes an order-independent fingerprint of a backup's **data** content (logs + goals
 * + preferences), excluding volatile metadata. Two databases with exactly the same content
 * produce the same fingerprint regardless of record or field ordering, so equality can be
 * decided with a single hash comparison instead of a record-by-record diff.
 *
 * Pure and dependency-free so it stays identical for local and remote inputs (the whole
 * point of the comparison) and is trivially unit-testable.
 *
 * Stability contract: the canonical form below defines the fingerprint for the **current**
 * schema. If it changes, existing remote backups will stop matching — bump deliberately and
 * add a new versioned golden test rather than silently altering it.
 */
object BackupContentFingerprint {

    /** Fingerprint of a bare data block: `{ history, daily_goals, preferences }`. */
    fun ofData(data: JsonObject): String = sha256(canonicalize(data))

    /**
     * Fingerprint of a full remote envelope `{ metadata, data }`. Metadata is ignored;
     * falls back to treating [raw] itself as the data block for legacy flat files.
     */
    fun ofRemoteEnvelope(raw: JsonObject): String =
        ofData(raw["data"]?.jsonObject ?: raw)

    /**
     * Deterministic string form of a JSON element:
     * - object keys are sorted, so field ordering is irrelevant;
     * - array elements are canonicalized then sorted, so record ordering is irrelevant.
     */
    private fun canonicalize(element: JsonElement): String = when (element) {
        is JsonObject -> element.entries
            .sortedBy { it.key }
            .joinToString(",", "{", "}") { (k, v) -> "$k:${canonicalize(v)}" }

        is JsonArray -> element
            .map { canonicalize(it) }
            .sorted()
            .joinToString(",", "[", "]")

        else -> element.toString()
    }

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
