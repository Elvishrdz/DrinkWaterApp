@file:Suppress("MagicNumber")

package com.eahm.drinkwaterapp.feature.dev.screenshots

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.entity.Status

/**
 * A single history record, positioned relative to today (the anchor day).
 *
 * @param daysAgo 0 = today, 1 = yesterday, etc. Records land on and behind today.
 */
data class PresetHistory(
    val daysAgo: Int,
    val hour: Int,
    val minute: Int,
    val amountMl: Float,
    val status: Status = Status.IN_APP,
)

/** A goal change, positioned relative to today. */
data class PresetGoal(
    val daysAgo: Int,
    val goalMl: Float,
)

/** Reminder configuration applied to [com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage] on insert. */
data class PresetReminders(
    val startTime: ActiveTime,
    val endTime: ActiveTime,
    val interval: ActiveTime,
    val amountPerDrinkMl: Float,
    /** Active days-of-week as ISO values (1 = Mon … 7 = Sun). */
    val activeDays: Set<Int>,
)

data class ScreenshotPreset(
    val history: List<PresetHistory>,
    val goals: List<PresetGoal>,
    val reminders: PresetReminders,
)

/**
 * Hand-authored dataset inserted by the "Insert preset dataset" button. Edit freely — values are
 * positioned relative to today. The oldest `daysAgo` represents the app install day; keep a goal at
 * (or before) that day so stats/streaks have a goal from day one.
 */
val screenshotPreset = ScreenshotPreset(
    history = listOf(
        // Today
        PresetHistory(daysAgo = 0, hour = 8, minute = 30, amountMl = 50f),
        PresetHistory(daysAgo = 0, hour = 11, minute = 0, amountMl = 150f),
        PresetHistory(daysAgo = 0, hour = 14, minute = 15, amountMl = 250f),
        PresetHistory(daysAgo = 0, hour = 17, minute = 45, amountMl = 150f),
        // 1 day ago
        PresetHistory(daysAgo = 1, hour = 9, minute = 0, amountMl = 100f),
        PresetHistory(daysAgo = 1, hour = 13, minute = 0, amountMl = 500f),
        PresetHistory(daysAgo = 1, hour = 19, minute = 0, amountMl = 250f),
        PresetHistory(daysAgo = 1, hour = 20, minute = 0, amountMl = 80f),
        // 2 days ago
        PresetHistory(daysAgo = 2, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 2, hour = 12, minute = 30, amountMl = 300f),
        PresetHistory(daysAgo = 2, hour = 16, minute = 0, amountMl = 250f),
        PresetHistory(daysAgo = 2, hour = 21, minute = 0, amountMl = 250f),
        // 3 days ago
        PresetHistory(daysAgo = 3, hour = 8, minute = 0, amountMl = 100f),
        PresetHistory(daysAgo = 3, hour = 12, minute = 30, amountMl = 200f),
        PresetHistory(daysAgo = 3, hour = 16, minute = 0, amountMl = 100f),
        PresetHistory(daysAgo = 3, hour = 21, minute = 0, amountMl = 150f),
        // 4 days ago
        PresetHistory(daysAgo = 4, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 4, hour = 12, minute = 30, amountMl = 200f),
        PresetHistory(daysAgo = 4, hour = 16, minute = 0, amountMl = 100f),
        PresetHistory(daysAgo = 4, hour = 21, minute = 0, amountMl = 50f),
        // 5 days ago
        PresetHistory(daysAgo = 5, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 5, hour = 12, minute = 30, amountMl = 200f),
        PresetHistory(daysAgo = 5, hour = 21, minute = 0, amountMl = 50f),
        // 6 days ago
        PresetHistory(daysAgo = 6, hour = 8, minute = 0, amountMl = 80f),
        PresetHistory(daysAgo = 6, hour = 12, minute = 30, amountMl = 240f),
        PresetHistory(daysAgo = 6, hour = 21, minute = 0, amountMl = 40f),
    ),
    goals = listOf(
        // Oldest goal == oldest history day → defines the app install date.
        PresetGoal(daysAgo = 6, goalMl = 1000f),
        PresetGoal(daysAgo = 0, goalMl = 1500f),
    ),
    reminders = PresetReminders(
        startTime = ActiveTime(hour = 8, minute = 0),
        endTime = ActiveTime(hour = 19, minute = 0),
        interval = ActiveTime(hour = 2, minute = 0),
        amountPerDrinkMl = 250f,
        activeDays = setOf(1, 2, 3, 4, 5),
    ),
)
