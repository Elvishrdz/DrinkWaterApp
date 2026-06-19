package com.eahm.drinkwaterapp.feature.dev.stresstest

import kotlinx.datetime.LocalDate

/** How goal records are laid out across the generated day range. */
enum class GoalMode {
    /** A single goal at the oldest day (the default value), carried forward to every day. */
    DEFAULT,

    /** A random goal at the oldest day, plus random goal changes scattered across all days. */
    RANDOM,

    /** Like [RANDOM], but goal changes only occur within a [day window][StressTestDevState.Data.rangeStartDay]. */
    RANDOM_IN_RANGE,
}

sealed interface StressTestDevState {
    data object Loading : StressTestDevState

    /**
     * @param anchorDate the most recent day to generate; data is generated backwards from here.
     * @param days how many days (including the anchor) to populate.
     * @param goalMode strategy for laying out goal records.
     * @param defaultGoalMl goal value used by [GoalMode.DEFAULT] and as a fallback.
     * @param minGoalMl/[maxGoalMl] inclusive bounds for random goal values.
     * @param changeEveryDays approximate cadence of goal changes in the random modes.
     * @param rangeStartDay/[rangeEndDay] day-offset window (0 = oldest) for [GoalMode.RANDOM_IN_RANGE].
     * @param isRunning whether a job is in progress (disables the buttons).
     * @param progress rows written so far in the current job.
     * @param total total rows the current job will write.
     * @param resultMessage human-readable outcome of the last action.
     */
    data class Data(
        val anchorDate: LocalDate,
        val days: Int = 365,
        val goalMode: GoalMode = GoalMode.RANDOM,
        val defaultGoalMl: Int = 2000,
        val minGoalMl: Int = 1500,
        val maxGoalMl: Int = 3000,
        val changeEveryDays: Int = 30,
        val rangeStartDay: Int = 0,
        val rangeEndDay: Int = 0,
        val isRunning: Boolean = false,
        val progress: Int = 0,
        val total: Int = 0,
        val resultMessage: String = "",
    ) : StressTestDevState
}
