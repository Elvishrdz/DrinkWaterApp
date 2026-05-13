package com.eahm.drinkwaterapp.core.data.goalreachedwidget

import android.content.Context
import android.content.Intent

fun GoalReachedWidget.Companion.createIntent(
    context: Context,
    style: CelebrationStyle,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
): Intent {
    return Intent(context, GoalReachedWidget::class.java).apply {
        putExtra(OVERLAY_STYLE, style)
        putExtra(OVERLAY_GOAL_ML, goalMl)
        putExtra(OVERLAY_DRINK_COUNT, drinkCount)
        putExtra(OVERLAY_STREAK_DAYS, streakDays)
    }
}

