package com.eahm.drinkwaterapp.feature.reminders

@Suppress("MagicNumber")
data class RemindersState(
    // ISO day-of-week values that are active: 1=Mon, 2=Tue, …, 7=Sun.
    val activeDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
)
