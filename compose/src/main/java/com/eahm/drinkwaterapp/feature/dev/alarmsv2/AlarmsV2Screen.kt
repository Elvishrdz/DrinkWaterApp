package com.eahm.drinkwaterapp.feature.dev.alarmsv2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.AlarmCountdown
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownConfig
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownUnit
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton

/** Inexact alarms within this window are considered "about to fire". */
private const val INEXACT_THRESHOLD_MS = 10_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmsV2Screen(
    state: AlarmsV2State,
    snackbarHostState: SnackbarHostState,
    onAction: (AlarmsV2Action) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms — Dev Dashboard") },
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
                AlarmsV2State.Loading -> CircularProgressIndicator()
                is AlarmsV2State.Data -> DataContent(state, onAction)
            }
        }
    }
}

@Composable
private fun DataContent(
    state: AlarmsV2State.Data,
    onAction: (AlarmsV2Action) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // Live clock
        Text(
            text = state.currentTime,
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )

        HorizontalDivider()

        // ── V2 Exact Alarm (primary) ─────────────────────────────────────────
        SectionTitle("Exact Alarm (primary)")
        StatusRow(label = "Active", value = state.exactAlarmActive)
        AlarmCountdownRow(
            scheduledAt = state.exactScheduledAt,
            countdownMs = state.exactCountdownMs,
            postFireLabel = "will trigger soon",
        )

        HorizontalDivider()

        // ── V2 Inexact Alarm (guard / fallback) ──────────────────────────────
        SectionTitle("Inexact Alarm (guard / fallback)")
        StatusRow(label = "Active", value = state.inexactAlarmActive)
        AlarmCountdownRow(
            scheduledAt = state.inexactScheduledAt,
            countdownMs = state.inexactCountdownMs,
            postFireLabel = "will trigger anytime from now",
        )
        state.inexactEarlyMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider()

        // ── V1 Drink Alarm (legacy) ──────────────────────────────────────────
        SectionTitle("V1 Drink Alarm (legacy)")
        StatusRow(label = "Active", value = state.v1AlarmActive)
        if (!state.v1AlarmActive) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.SetupV1Alarm) },
            ) {
                Text("Start V1 Alarm")
            }
        }

        if (state.v1AlarmActive && state.v1AlarmStorageInfo != null
            && state.v1AlarmStorageInfo.alarmTriggerAtMillis != -1L
        ) {
            val info = state.v1AlarmStorageInfo
            InfoRow(
                label = "Next at",
                value = "%02d:%02d".format(
                    info.alarmTriggerAt.hour,
                    info.alarmTriggerAt.minute,
                ),
            )
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
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.StopV1Alarm) },
            ) {
                Text("Stop V1 Alarm")
            }
        }

        state.v1AlarmStorageInfo?.let { info ->
            Spacer(Modifier.height(4.dp))
            InfoRow("Alarm ID", "${info.alarmId}")
            InfoRow("Trigger time", "${info.alarmTriggerTime}")
            InfoRow("Trigger time (ms)", "${info.alarmTriggerTimeMillis}")
            InfoRow("Trigger at", "${info.alarmTriggerAt}")
            InfoRow("Trigger at (ms)", "${info.alarmTriggerAtMillis}")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(AlarmsV2Action.CallOnResume) },
        ) {
            Text("Call on resume now")
        }

        HorizontalDivider()

        // ── System State ─────────────────────────────────────────────────────
        SectionTitle("System State")
        StatusRow(label = "Exact alarm permission", value = state.exactPermissionGranted)
        StatusRow(label = "Notification permission", value = state.hasNotificationPermissions)
        if (!state.hasNotificationPermissions) {
            RequestNotificationPermissionButton(
                onGranted = { onAction(AlarmsV2Action.NotificationPermissionGranted) },
                onOpenSettings = { onAction(AlarmsV2Action.OpenAppPermissionSettings) },
            )
        }

        StatusRow(label = "Within active hours (06:00–18:00)", value = state.isWithinActiveHours)
        InfoRow(label = "Next computed slot", value = state.nextSlot)

        HorizontalDivider()

        // ── Test Alarm — AlarmSchedulerV2 ────────────────────────────────────
        SectionTitle("Test Alarm — AlarmSchedulerV2")
        InfoRow(
            label = "Alarm type",
            value = if (state.exactPermissionGranted) "EXACT (setExactAndAllowWhileIdle)"
            else "INEXACT (setAndAllowWhileIdle)",
        )

        AppTimeSelectorButton(
            text = "Alarm time: %02d:%02d".format(
                state.selectedTestTime.hour,
                state.selectedTestTime.minute,
            ),
            time = state.selectedTestTime,
            onTimeSelected = { onAction(AlarmsV2Action.OnTestAlarmTimeSelected(it)) },
        )

        state.scheduledAsExact?.let { wasExact ->
            InfoRow(label = "Scheduled as", value = if (wasExact) "EXACT" else "INEXACT")
        }

        TestAlarmCountdownDisplay(
            countdownMs = state.testCountdownMs,
            scheduledAsExact = state.scheduledAsExact,
        )

        Spacer(Modifier.height(4.dp))

        if (!state.isTestAlarmActive) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.ScheduleTestAlarm) },
            ) {
                Text("Schedule test alarm")
            }
        } else {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.CancelTestAlarm) },
            ) {
                Text("Cancel test alarm")
            }
        }

        state.lastPermissionEvent?.let {
            Text(
                text = "Last broadcast: $it",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(AlarmsV2Action.Refresh) },
        ) {
            Text("Refresh permission state")
        }

        HorizontalDivider()

        // ── Actions — V2 Dual Alarm ──────────────────────────────────────────
        SectionTitle("Actions — V2 Dual Alarm")

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(AlarmsV2Action.StartAlarms) },
        ) {
            Text("Start / Restore Alarms")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(AlarmsV2Action.StopAlarms) },
        ) {
            Text("Stop Alarms")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(AlarmsV2Action.Refresh) },
        ) {
            Text("Refresh")
        }

        if (!state.exactPermissionGranted) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.OpenExactPermissionSettings) },
            ) {
                Text("Open Exact Alarm Permission Settings")
            }
        }
    }
}

// region Sub-composables

@Composable
private fun AlarmCountdownRow(
    scheduledAt: String?,
    countdownMs: Long?,
    postFireLabel: String,
) {
    when {
        scheduledAt == null || countdownMs == null -> {
            InfoRow(label = "Scheduled at", value = "—")
            InfoRow(label = "Triggers in", value = "—")
        }

        countdownMs == 0L -> {
            InfoRow(label = "Scheduled at", value = scheduledAt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Triggers in", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = postFireLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100),
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        else -> {
            InfoRow(label = "Scheduled at", value = scheduledAt)
            InfoRow(label = "Triggers in", value = formatCountdown(countdownMs))
        }
    }
}

@Composable
private fun TestAlarmCountdownDisplay(
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
    ) { isGranted ->
        if (isGranted) onGranted() else goToSettings = true
    }

    val requestPermission: () -> Unit = {
        val status =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (status == PackageManager.PERMISSION_GRANTED) onGranted()
        else launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    TextButton(
        onClick = { if (goToSettings) onOpenSettings() else requestPermission() },
    ) {
        Text(if (goToSettings) "Grant in settings" else "Request")
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun StatusRow(label: String, value: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = if (value) "YES" else "NO",
            fontWeight = FontWeight.Bold,
            color = if (value) Color(0xFF2E7D32) else Color(0xFFC62828),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

// endregion

// region Formatters

private fun formatCountdown(ms: Long): String {
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    val seconds = (ms % 60_000) / 1_000
    return when {
        hours > 0 -> "${hours}h %02dm %02ds".format(minutes, seconds)
        minutes > 0 -> "${minutes}m %02ds".format(seconds)
        else -> "${seconds}s"
    }
}

private fun formatExactAlarmCountdown(ms: Long): String {
    val totalSeconds = ms / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

// endregion
