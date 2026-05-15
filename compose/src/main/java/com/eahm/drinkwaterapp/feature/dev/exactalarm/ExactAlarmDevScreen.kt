package com.eahm.drinkwaterapp.feature.dev.exactalarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExactAlarmDevScreen(
    state: ExactAlarmDevState,
    onAction: (ExactAlarmDevAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exact Alarm Permission") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text("← Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopStart,
        ) {
            when (state) {
                ExactAlarmDevState.Loading -> CircularProgressIndicator()
                is ExactAlarmDevState.Data -> Content(state, onAction)
            }
        }
    }
}

@Composable
private fun Content(
    state: ExactAlarmDevState.Data,
    onAction: (ExactAlarmDevAction) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // --- Alarm type info ---
        Text("Alarm type", style = MaterialTheme.typography.titleSmall)
        Text("Scheduled as: ${if (state.isPermissionGranted) "EXACT (setExactAndAllowWhileIdle)" else "INEXACT (setAndAllowWhileIdle)"}")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Alarm section ---
        Text("AlarmSchedulerV2 test", style = MaterialTheme.typography.titleSmall)

        AppTimeSelectorButton(
            text = "Alarm time: %02d:%02d".format(
                state.selectedTime.hour,
                state.selectedTime.minute
            ),
            time = state.selectedTime,
            onTimeSelected = { onAction(ExactAlarmDevAction.OnTimeSelected(it)) },
        )

        state.scheduledAsExact?.let { wasExact ->
            Text("Scheduled as: ${if (wasExact) "EXACT" else "INEXACT"}")
        }

        CountdownDisplay(
            countdownMs = state.countdownMs,
            scheduledAsExact = state.scheduledAsExact,
        )

        Spacer(Modifier.height(4.dp))

        if (!state.isTestAlarmActive) {
            DwPrimaryButton(
                text = "Schedule test alarm",
                onClick = { onAction(ExactAlarmDevAction.ScheduleTestAlarm) },
            )
        } else {
            DwPrimaryButton(
                text = "Cancel test alarm",
                onClick = { onAction(ExactAlarmDevAction.CancelTestAlarm) },
            )
        }

        // --- Permission section ---
        Text("Permission", style = MaterialTheme.typography.titleSmall)
        Text("SCHEDULE_EXACT_ALARM granted: ${state.isPermissionGranted}")

        state.lastPermissionEvent?.let {
            Text(
                text = "Last broadcast event: $it",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(4.dp))

        DwPrimaryButton(
            text = "Open Permission Settings",
            onClick = { onAction(ExactAlarmDevAction.OpenPermissionSettings) },
        )

        DwPrimaryButton(
            text = "Refresh permission state",
            onClick = { onAction(ExactAlarmDevAction.RefreshPermissionState) },
        )
    }
}

@Composable
private fun CountdownDisplay(
    countdownMs: Long?,
    scheduledAsExact: Boolean?,
) {
    if (countdownMs == null) return

    val isExact = scheduledAsExact == true
    val text = when {
        countdownMs <= 0 && isExact -> "Alarm will arrive soon"
        countdownMs <= 0 -> "Alarm will arrive at any moment now"
        !isExact && countdownMs <= INEXACT_THRESHOLD_MS -> "Alarm will arrive at any moment now"
        else -> "Fires in ${formatCountdown(countdownMs)}${if (!isExact) " (approximate)" else ""}"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

private fun formatCountdown(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentCountingDownPreview() {
    Content(
        state = ExactAlarmDevState.Data(
            isPermissionGranted = true,
            isTestAlarmActive = true,
            scheduledAsExact = true,
            selectedTime = TimeSelected(14, 30),
            countdownMs = 47_000,
            lastPermissionEvent = "GRANTED — alarm rescheduled inexact → exact",
        ),
        onAction = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ContentInexactNearPreview() {
    Content(
        state = ExactAlarmDevState.Data(
            isPermissionGranted = false,
            isTestAlarmActive = true,
            scheduledAsExact = false,
            selectedTime = TimeSelected(14, 30),
            countdownMs = 6_000,
            lastPermissionEvent = "REVOKED — alarm rescheduled exact → inexact",
        ),
        onAction = {},
    )
}
