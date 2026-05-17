package com.eahm.drinkwaterapp.feature.dev.alarms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.eahm.drinkwaterapp.core.data.drinkalarm.data.TimeResult
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.pad
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.AlarmCountdown
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownConfig
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownUnit
import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmsScreen(
    state: AlarmsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (AlarmsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) { Text("← Back") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is AlarmsState.Loading -> CircularProgressIndicator()
                is AlarmsState.Data -> DataContent(state, onAction)
            }
        }
    }
}

@Composable
private fun DataContent(state: AlarmsState.Data, onAction: (AlarmsAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.canScheduleExactAlarms.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Can schedule exact alarms: $it",
                    color = if (it) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.error,
                    fontWeight = if (it) FontWeight.Normal
                    else FontWeight.Bold,
                )

                if (!it) {
                    RequestScheduleExactAlarmsAuthorizationButton()
                }
            }
        }

        state.hasNotificationPermissions.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Has notification permission: $it",
                    color = if (it) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.error,
                    fontWeight = if (it) FontWeight.Normal
                    else FontWeight.Bold,
                )

                if (!it) {
                    RequestNotificationPermissionButton(
                        onGranted = { onAction(AlarmsAction.NotificationPermissionGranted) },
                        onOpenSettings = { onAction(AlarmsAction.OpenAppPermissionSettings) },
                    )
                }
            }
        }

        state.isAlarmActive.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Is drink alarm active: $it",
                    color = if (it) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.error,
                    fontWeight = if (it) FontWeight.Normal
                    else FontWeight.Bold,
                )

                if (!it) {
                    DwTertiaryButton(
                        text = "Start Alarm",
                        onClick = { onAction(AlarmsAction.SetupAlarm) },
                    )
                }
            }
        }

        Text(
            text = "In active hours: ${state.inActiveHours}",
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(16.dp))

        state.alarmStorageInfo?.let {
            AlarmCountdownContent(
                isAlarmActive = state.isAlarmActive,
                info = it,
                onAction = onAction,
            )
        }

        Spacer(Modifier.height(16.dp))

        state.alarmStorageInfo?.let {
            Column {
                Text("Alarm id: ${it.alarmId}")
                Text("Alarm trigger time: ${it.alarmTriggerTime}")
                Text("Alarm trigger time (millis): ${it.alarmTriggerTimeMillis}")
                Text("Alarm trigger at: ${it.alarmTriggerAt}")
                Text("Alarm trigger at (millis): ${it.alarmTriggerAtMillis}")
            }
        }

        DwPrimaryButton(
            text = "Call on resume now",
            onClick = { onAction(AlarmsAction.OnResumeCalled) },
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Exact Alarm Dev",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        ExactAlarmSection(state, onAction)
    }
}

@Composable
private fun RequestScheduleExactAlarmsAuthorizationButton() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val context = LocalContext.current

    DwTertiaryButton(
        text = "Grant authorization in app settings",
        onClick = {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        },
    )
}

@Composable
private fun RequestNotificationPermissionButton(
    onGranted: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var goToSettings by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) onGranted() else goToSettings = true
    }

    val requestPermission: () -> Unit = {
        val status = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )
        when (PackageManager.PERMISSION_GRANTED) {
            status -> onGranted()
            else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DwTertiaryButton(
        text = if (goToSettings) "Grant permission in app settings" else "Request Permission",
        onClick = { if (goToSettings) onOpenSettings() else requestPermission() },
    )
}

@Composable
private fun AlarmCountdownContent(
    isAlarmActive: Boolean,
    info: AlarmStorageInfo,
    onAction: (AlarmsAction) -> Unit,
) {
    if (isAlarmActive && info.alarmTriggerAtMillis != -1L) {
        Text(
            text = "Next alarm at ${info.alarmTriggerAt.hour.pad()}:${info.alarmTriggerAt.minute.pad()}",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = DwInk500,
        )
        Spacer(Modifier.height(12.dp))
        AlarmCountdown(
            triggerAtElapsedMs = info.alarmTriggerAtMillis,
            config = CountdownConfig(
                units = setOf(
                    CountdownUnit.HOURS,
                    CountdownUnit.MINUTES,
                    CountdownUnit.SECONDS,
                ),
                hideLeadingZeroUnits = true,
            ),
        )
        DwPrimaryButton(
            text = "Stop alarm",
            onClick = { onAction(AlarmsAction.StopAlarm) },
        )
    } else {
        Text(
            text = "No active alarm",
            fontFamily = PlusJakartaSans,
            color = DwInk500,
        )
    }
}

@Composable
private fun ExactAlarmSection(state: AlarmsState.Data, onAction: (AlarmsAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // --- Alarm type info ---
        Text("Alarm type", style = MaterialTheme.typography.titleSmall)
        Text(
            "Scheduled as: ${
                if (state.canScheduleExactAlarms) "EXACT (setExactAndAllowWhileIdle)"
                else "INEXACT (setAndAllowWhileIdle)"
            }"
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- AlarmSchedulerV2 test ---
        Text("AlarmSchedulerV2 test", style = MaterialTheme.typography.titleSmall)

        AppTimeSelectorButton(
            text = "Alarm time: %02d:%02d".format(
                state.selectedTime.hour,
                state.selectedTime.minute,
            ),
            time = state.selectedTime,
            onTimeSelected = { onAction(AlarmsAction.OnTestAlarmTimeSelected(it)) },
        )

        state.scheduledAsExact?.let { wasExact ->
            Text("Scheduled as: ${if (wasExact) "EXACT" else "INEXACT"}")
        }

        ExactAlarmCountdownDisplay(
            countdownMs = state.countdownMs,
            scheduledAsExact = state.scheduledAsExact,
        )

        Spacer(Modifier.height(4.dp))

        if (!state.isTestAlarmActive) {
            DwPrimaryButton(
                text = "Schedule test alarm",
                onClick = { onAction(AlarmsAction.ScheduleTestAlarm) },
            )
        } else {
            DwPrimaryButton(
                text = "Cancel test alarm",
                onClick = { onAction(AlarmsAction.CancelTestAlarm) },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // --- Permission ---
        Text("Permission", style = MaterialTheme.typography.titleSmall)
        Text("SCHEDULE_EXACT_ALARM granted: ${state.canScheduleExactAlarms}")

        state.lastPermissionEvent?.let {
            Text(
                text = "Last broadcast event: $it",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(4.dp))

        DwPrimaryButton(
            text = "Open Permission Settings",
            onClick = { onAction(AlarmsAction.OpenExactAlarmPermissionSettings) },
        )

        DwPrimaryButton(
            text = "Refresh permission state",
            onClick = { onAction(AlarmsAction.RefreshPermissionState) },
        )
    }
}

@Composable
private fun ExactAlarmCountdownDisplay(
    countdownMs: Long?,
    scheduledAsExact: Boolean?,
) {
    if (countdownMs == null) return

    val isExact = scheduledAsExact == true
    val text = when {
        countdownMs <= 0 && isExact -> "Alarm will arrive soon"
        countdownMs <= 0 -> "Alarm will arrive at any moment now"
        !isExact && countdownMs <= INEXACT_THRESHOLD_MS -> "Alarm will arrive at any moment now"
        else -> "Fires in ${formatExactAlarmCountdown(countdownMs)}${if (!isExact) " (approximate)" else ""}"
    }

    Text(text = text, style = MaterialTheme.typography.bodyLarge)
}

private fun formatExactAlarmCountdown(ms: Long): String {
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

@Preview
@Composable
private fun ActiveAlarmPreview() {
    AlarmsScreen(
        state = AlarmsState.Data(
            canScheduleExactAlarms = false,
            hasNotificationPermissions = true,
            isAlarmActive = true,
            alarmStorageInfo = AlarmStorageInfo(
                alarmId = 127,
                alarmTriggerTimeMillis = 60_000L,
                alarmTriggerTime = TimeResult(0, 1, 0, 0),
                alarmTriggerAtMillis = SystemClock.elapsedRealtime() + 3_661_000L,
                alarmTriggerAt = TimeResult(15, 30, 1, 0),
            ),
            inActiveHours = false,
            isTestAlarmActive = false,
            scheduledAsExact = null,
            countdownMs = null,
            selectedTime = TimeSelected(14, 30),
            lastPermissionEvent = null,
        ),
        onAction = {},
        onBackClicked = {},
    )
}

@Preview
@Composable
private fun NoActiveAlarmPreview() {
    AlarmsScreen(
        state = AlarmsState.Data(
            canScheduleExactAlarms = false,
            hasNotificationPermissions = false,
            isAlarmActive = false,
            alarmStorageInfo = null,
            inActiveHours = false,
            isTestAlarmActive = true,
            scheduledAsExact = true,
            countdownMs = 47_000,
            selectedTime = TimeSelected(14, 30),
            lastPermissionEvent = "GRANTED — alarm rescheduled inexact → exact",
        ),
        onAction = {},
        onBackClicked = {},
    )
}
