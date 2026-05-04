package com.eahm.drinkwaterapp.feature.dev.alarms.countdown

/**
 * Controls which time units are visible in [AlarmCountdown] and how zeros are handled.
 *
 * @param units The set of [CountdownUnit]s to decompose the remaining time into.
 *   Non-included units roll their value into the next smaller included unit.
 *   Examples:
 *   - `setOf(HOURS, MINUTES, SECONDS)` — shows total hours, remaining minutes, remaining seconds.
 *   - `setOf(MINUTES)` — shows the total whole minutes remaining (no seconds, no hours).
 *   - `setOf(DAYS, HOURS, MINUTES, SECONDS)` — whole days with hms breakdown.
 *
 * @param hideLeadingZeroUnits When true, units at the large end that have a value of zero
 *   are omitted until the first non-zero unit.
 *   Example: 0h 10m 15s → shown as "10m 15s".
 *   Always shows at least the smallest unit, even if it is zero.
 */
data class CountdownConfig(
    val units: Set<CountdownUnit> = CountdownUnit.Default,
    val hideLeadingZeroUnits: Boolean = true,
)
