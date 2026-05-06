package com.eahm.drinkwaterapp.core.domain.extension

import androidx.compose.ui.util.fastRoundToInt
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult

fun Int.createTimeResultFromSeconds(): TimeResult {
    val totalMinutes = this.toFloat() / 60
    val roundedTotalMinutes = totalMinutes.fastRoundToInt()
    val hours = ((roundedTotalMinutes / 60) % 24).coerceAtMost(23)
    val minutes = (roundedTotalMinutes % 60).coerceAtMost(59)
    val roundedSeconds = roundedTotalMinutes * 60
    val millis = roundedSeconds * 1000L

    return TimeResult(
        hours = hours,
        minutes = minutes,
        seconds = roundedSeconds,
        millis = millis,
    )
}
