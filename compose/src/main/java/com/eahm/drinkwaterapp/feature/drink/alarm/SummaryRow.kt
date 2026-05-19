package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

@Composable
internal fun SummaryRow(
    schedule: DrinkSchedule?,
    interval: AlarmInterval,
    nextAlarmSlot: LocalDateTime?,
    isInActiveHours: Boolean,
    isInexact: Boolean = false,
    is24Hour: Boolean = true,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (schedule != null) {
            SummaryChip(
                label = stringResource(R.string.drink_alarm_summary_drinks),
                value = "${schedule.count}"
            )

            val totalMin = interval.hour * 60 + interval.minute
            val h = totalMin / 60
            val m = totalMin % 60
            val intervalLabel = buildString {
                if (h > 0) append("${h}h ")
                if (m > 0 || h == 0) append("${m}m")
            }.trim()
            SummaryChip(
                label = stringResource(
                    if (isInexact) R.string.drink_alarm_summary_every_approx
                    else R.string.drink_alarm_summary_every
                ),
                value = intervalLabel
            )
        }

        nextAlarmSlot?.let { next ->
            val label = when {
                !isInActiveHours -> stringResource(R.string.drink_alarm_summary_starts)
                isInexact -> stringResource(R.string.drink_alarm_summary_next_approx)
                else -> stringResource(R.string.drink_alarm_summary_next)
            }
            SummaryChip(
                label = label,
                value = next.formatTime(is24Hour),
            )
        }
    }
}

private fun LocalDateTime.formatTime(is24Hour: Boolean): String {
    val mm = minute.toString().padStart(2, '0')
    return if (is24Hour) {
        "${hour.toString().padStart(2, '0')}:$mm"
    } else {
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val period = if (hour < 12) "AM" else "PM"
        "$h12:$mm $period"
    }
}

@Preview
@Composable
private fun SummaryRowPreview() {
    DrinkWaterAppTheme {
        SummaryRow(
            drinkScheduleMock,
            interval = AlarmInterval(0, 5),
            isInActiveHours = true,
            nextAlarmSlot = LocalDateTime(2026, 5, 10, 20, 0),
        )
    }
}
