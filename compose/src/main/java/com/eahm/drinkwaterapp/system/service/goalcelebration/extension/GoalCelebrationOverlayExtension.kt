package com.eahm.drinkwaterapp.system.service.goalcelebration.extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.eahm.drinkwaterapp.core.domain.extension.startImageSendIntent
import com.eahm.drinkwaterapp.core.domain.extension.startTextSendIntent
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.system.service.goalcelebration.GoalCelebrationOverlay
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import java.io.File
import java.io.FileOutputStream

fun GoalCelebrationOverlay.Companion.createIntent(
    context: Context,
    style: CelebrationStyle,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
    unitPreference: UnitPreference = UnitPreference.ML,
): Intent {
    return Intent(context, GoalCelebrationOverlay::class.java).apply {
        putExtra(OVERLAY_STYLE, style)
        putExtra(OVERLAY_GOAL_ML, goalMl)
        putExtra(OVERLAY_DRINK_COUNT, drinkCount)
        putExtra(OVERLAY_STREAK_DAYS, streakDays)
        putExtra(OVERLAY_UNIT_PREFERENCE, unitPreference.name)
    }
}

internal fun GoalCelebrationOverlay.shareBitmap(
    bitmap: Bitmap,
    shareText: String,
    fileName: String,
) {
    try {
        // "shared_images" must match with file_path.xml used in the AndroidManifest.xml
        val imagesDir = File(cacheDir, "shared_images").also { it.mkdirs() }
        val imageFile = File(imagesDir, fileName)

        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val imageUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            imageFile,
        )

        startImageSendIntent(imageUri, shareText)

    } catch (_: Exception) {
        // Fallback to text-only share if image export fails
        startTextSendIntent(shareText)
    }
}
