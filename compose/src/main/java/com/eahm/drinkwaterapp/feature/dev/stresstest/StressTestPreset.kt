@file:Suppress("MagicNumber")

package com.eahm.drinkwaterapp.feature.dev.stresstest

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime
import com.eahm.drinkwaterapp.core.database.entity.Status

/**
 * A single history record, positioned relative to the anchor day chosen in the dev menu.
 *
 * @param daysAgo 0 = the anchor day, 1 = the day before, etc. Records land on and behind the anchor.
 */
data class PresetHistory(
    val daysAgo: Int,
    val hour: Int,
    val minute: Int,
    val amountMl: Float,
    val status: Status = Status.IN_APP,
)

/** A goal change, positioned relative to the anchor day. */
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

data class StressTestPreset(
    val history: List<PresetHistory>,
    val goals: List<PresetGoal>,
    val reminders: PresetReminders,
)

/**
 * Hand-authored dataset inserted by the "Insert preset dataset" button. Edit freely, values are
 * positioned relative to the anchor date selected in the dev menu. The oldest `daysAgo` represents
 * the app install day; keep a goal at (or before) that day so stats/streaks have a goal from day one.
 */
val stressTestPreset = StressTestPreset(
    history = listOf(
        // Anchor day (today by default)
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
        // 7 days ago
        PresetHistory(daysAgo = 7, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 7, hour = 12, minute = 30, amountMl = 250f),
        PresetHistory(daysAgo = 7, hour = 21, minute = 0, amountMl = 50f),
        // 8 days ago
        PresetHistory(daysAgo = 8, hour = 8, minute = 0, amountMl = 150f),
        PresetHistory(daysAgo = 8, hour = 10, minute = 0, amountMl = 250f),
        PresetHistory(daysAgo = 8, hour = 12, minute = 30, amountMl = 100f),
        PresetHistory(daysAgo = 8, hour = 16, minute = 30, amountMl = 150f),
        PresetHistory(daysAgo = 8, hour = 19, minute = 0, amountMl = 150f),
        PresetHistory(daysAgo = 8, hour = 21, minute = 0, amountMl = 250f),
        PresetHistory(daysAgo = 8, hour = 21, minute = 30, amountMl = 30f),
        // 9 days ago
        PresetHistory(daysAgo = 9, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 9, hour = 12, minute = 30, amountMl = 100f),
        PresetHistory(daysAgo = 9, hour = 21, minute = 0, amountMl = 50f),
        // 10 days ago
        PresetHistory(daysAgo = 10, hour = 8, minute = 0, amountMl = 100f),
        PresetHistory(daysAgo = 10, hour = 12, minute = 30, amountMl = 200f),
        PresetHistory(daysAgo = 10, hour = 21, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 10, hour = 22, minute = 0, amountMl = 150f),
        // 11 days ago
        PresetHistory(daysAgo = 11, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 11, hour = 12, minute = 30, amountMl = 250f),
        PresetHistory(daysAgo = 11, hour = 21, minute = 0, amountMl = 50f),
        // 12 days ago
        PresetHistory(daysAgo = 12, hour = 8, minute = 0, amountMl = 50f),
        PresetHistory(daysAgo = 12, hour = 12, minute = 30, amountMl = 50f),
        PresetHistory(daysAgo = 12, hour = 21, minute = 0, amountMl = 50f),
    ),
    goals = listOf(
        // Oldest goal == oldest history day → defines the app install date.
        PresetGoal(daysAgo = 12, goalMl = 1000f),
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
