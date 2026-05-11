package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.data.drinkalarm.DrinkSchedule
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.domain.extension.pad
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

@Composable
internal fun SummaryRow(
    schedule: DrinkSchedule?,
    interval: AlarmInterval,
    nextAlarmSlot: LocalDateTime?,
    isInActiveHours: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (schedule != null) {
            SummaryChip(label = "Drinks", value = "${schedule.count}")

            val totalMin = interval.hour * 60 + interval.minute
            val h = totalMin / 60
            val m = totalMin % 60
            val intervalLabel = buildString {
                if (h > 0) append("${h}h ")
                if (m > 0 || h == 0) append("${m}m")
            }.trim()
            SummaryChip(label = "Every", value = intervalLabel)
        }

        nextAlarmSlot?.let { next ->
            val label = if (isInActiveHours) "Next" else "Starts"
            SummaryChip(
                label = label,
                value = "${next.hour.pad()}:${next.minute.pad()}",
            )
        }
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
