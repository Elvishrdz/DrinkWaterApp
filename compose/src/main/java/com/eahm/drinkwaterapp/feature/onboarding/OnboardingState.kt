package com.eahm.drinkwaterapp.feature.onboarding

import com.eahm.drinkwaterapp.core.data.datastore.data.ActiveTime


internal enum class Gender { FEMALE, MALE, PREFER_NOT_TO_SAY }
internal enum class WeightUnit { LB, KG }
internal enum class ActivityLevel { LIGHT, MODERATE, ACTIVE }

/** A time represented in 12-hour format with AM/PM. */
internal data class OnbTimeSelection(
    val hour: Int,    // 1–12
    val minute: Int,  // 0–59
    val isPm: Boolean,
) {
    fun toHour24(): Int = when {
        isPm && hour != 12 -> hour + 12
        !isPm && hour == 12 -> 0
        else -> hour
    }

    fun toActiveTime() = ActiveTime(hour = toHour24(), minute = minute)

    fun label(): String {
        val h = hour.toString().padStart(2, '0')
        val m = minute.toString().padStart(2, '0')
        val period = if (isPm) "PM" else "AM"
        return "$h:$m $period"
    }
}

internal data class OnboardingFormState(
    val step: Int = 1,
    val name: String = "",
    val gender: Gender? = null,
    val weightValue: String = "",
    val weightUnit: WeightUnit = WeightUnit.LB,
    val activityLevel: ActivityLevel? = null,
    val wakeTime: OnbTimeSelection = OnbTimeSelection(hour = 6, minute = 0, isPm = false),
    val sleepTime: OnbTimeSelection = OnbTimeSelection(hour = 10, minute = 0, isPm = true),
    val intervalMinutes: Int = 45,
    val computedGoalMl: Int = 2000,
    val notifAmountMl: Float = 150f,
)

sealed interface OnboardingSteps {

    data object Intro : OnboardingSteps
    data object DailyGoal : OnboardingSteps

    sealed interface RemindersSteps : OnboardingSteps {
        data object StartTime : RemindersSteps
        data object EndTime : RemindersSteps
        data object Interval : RemindersSteps
        data object Notification : RemindersSteps
    }

    data object Celebrations : OnboardingSteps

    data object Summary : OnboardingSteps

    companion object
}


internal val OnboardingSteps.Companion.totalSteps: Int get() = 8

fun OnboardingSteps.Companion.byStep(step: Int) = when (step) {
    1 -> OnboardingSteps.Intro
    2 -> OnboardingSteps.DailyGoal

    3 -> OnboardingSteps.RemindersSteps.StartTime
    4 -> OnboardingSteps.RemindersSteps.EndTime
    5 -> OnboardingSteps.RemindersSteps.Interval
    6 -> OnboardingSteps.RemindersSteps.Notification
    7 -> OnboardingSteps.Celebrations

    totalSteps -> OnboardingSteps.Summary
    else -> OnboardingSteps.Intro
}