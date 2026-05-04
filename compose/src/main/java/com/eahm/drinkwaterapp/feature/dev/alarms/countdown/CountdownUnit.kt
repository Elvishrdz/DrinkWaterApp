package com.eahm.drinkwaterapp.feature.dev.alarms.countdown

enum class CountdownUnit(val label: String, val inMs: Long) {
    YEARS("y", 365L * 24 * 60 * 60 * 1_000),
    MONTHS("mo", 30L * 24 * 60 * 60 * 1_000),
    WEEKS("w", 7L * 24 * 60 * 60 * 1_000),
    DAYS("d", 24L * 60 * 60 * 1_000),
    HOURS("h", 60L * 60 * 1_000),
    MINUTES("m", 60L * 1_000),
    SECONDS("s", 1_000L),
    ;

    companion object {
        val Default: Set<CountdownUnit> = setOf(DAYS, HOURS, MINUTES, SECONDS)
    }
}
