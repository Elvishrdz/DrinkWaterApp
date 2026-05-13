package com.eahm.drinkwaterapp.system.service.goalcelebration.extension

import android.content.Context
import android.content.Intent
import com.eahm.drinkwaterapp.system.service.goalcelebration.GoalCelebrationOverlay
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle

fun GoalCelebrationOverlay.Companion.createIntent(
    context: Context,
    style: CelebrationStyle,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
): Intent {
    return Intent(context, GoalCelebrationOverlay::class.java).apply {
        putExtra(OVERLAY_STYLE, style)
        putExtra(OVERLAY_GOAL_ML, goalMl)
        putExtra(OVERLAY_DRINK_COUNT, drinkCount)
        putExtra(OVERLAY_STREAK_DAYS, streakDays)
    }
}
