package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime

@Composable
internal fun ActiveScheduleCard(
    state: HomeAlarmState.Active,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .outlineBorderStyle()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reminders",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ActiveStatusBadge(isActive = state.isInActiveHours)
        }

        if (state.schedule != null) {
            HomeScheduleTimeline(
                schedule = state.schedule,
                startTime = state.startTime,
                endTime = state.endTime,
                now = state.now,
                isInActiveHours = state.isInActiveHours,
            )
        }

        SummaryRow(
            schedule = state.schedule,
            interval = state.interval,
            nextAlarmSlot = state.nextAlarmSlot,
            isInActiveHours = state.isInActiveHours,
        )
    }
}

@Preview
@Composable
private fun ActiveScheduleCardPreview() {
    DrinkWaterAppTheme {
        ActiveScheduleCard(
            state = HomeAlarmState.Active(
                startTime = LocalDateTime(2026, 5, 10, 8, 0),
                endTime = LocalDateTime(2026, 5, 10, 20, 0),
                interval = AlarmInterval(0, 90),
                schedule = null,
                nextAlarmSlot = LocalDateTime(2026, 5, 10, 14, 30),
                isInActiveHours = true,
                now = LocalDateTime(2026, 5, 10, 13, 15),
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}
