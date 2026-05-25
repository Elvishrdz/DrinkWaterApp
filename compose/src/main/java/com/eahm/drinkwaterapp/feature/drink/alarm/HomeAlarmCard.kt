package com.eahm.drinkwaterapp.feature.drink.alarm

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.domain.model.AlarmInterval
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeAlarmCard(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        ActiveScheduleCard(
            state = homeAlarmStateActiveMock,
            modifier = modifier,
        )
        return
    }

    val viewModel = koinViewModel<HomeAlarmViewModel>()

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        HomeAlarmState.Loading -> {}
        HomeAlarmState.NoPermission -> NoPermissionCard(modifier)
        HomeAlarmState.NotConfigured -> NotConfiguredCard(modifier, onNavigateToSettings)
        is HomeAlarmState.Active -> ActiveScheduleCard(state = s, modifier = modifier)
    }
}

@Preview
@Composable
private fun NoPermissionPreview() {
    DrinkWaterAppTheme {
        NoPermissionCard(Modifier.padding(20.dp))
    }
}

@Preview
@Composable
private fun NotConfiguredPreview() {
    DrinkWaterAppTheme {
        NotConfiguredCard(
            onNavigateToSettings = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Preview
@Composable
private fun ActiveInActiveHoursPreview() {
    DrinkWaterAppTheme {
        ActiveScheduleCard(
            state = homeAlarmStateActiveMock,
            modifier = Modifier.padding(20.dp),
        )
    }
}

private val homeAlarmStateActiveMock = HomeAlarmState.Active(
    startTime = LocalDateTime(2026, 5, 10, 8, 0),
    endTime = LocalDateTime(2026, 5, 10, 20, 0),
    interval = AlarmInterval(0, 90),
    schedule = drinkScheduleMock,
    nextAlarmSlot = LocalDateTime(2026, 5, 10, 14, 30),
    isInActiveHours = true,
    now = LocalDateTime(2026, 5, 10, 13, 15),
)

@Preview
@Composable
private fun ActiveRestingPreview() {
    DrinkWaterAppTheme {
        ActiveScheduleCard(
            state = HomeAlarmState.Active(
                startTime = LocalDateTime(2026, 5, 10, 8, 0),
                endTime = LocalDateTime(2026, 5, 10, 20, 0),
                interval = AlarmInterval(1, 30),
                schedule = drinkScheduleMock,
                nextAlarmSlot = LocalDateTime(2026, 5, 10, 8, 0),
                isInActiveHours = false,
                now = LocalDateTime(2026, 5, 10, 22, 0),
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}
