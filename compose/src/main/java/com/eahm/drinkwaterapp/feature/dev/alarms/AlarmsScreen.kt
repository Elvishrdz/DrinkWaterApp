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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.AlarmCountdown
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownConfig
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.CountdownUnit
import com.eahm.drinkwaterapp.feature.dev.alarms.countdown.pad
import com.eahm.drinkwaterapp.system.alarm.AlarmStorageInfo

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
    }
}

@Composable
private fun RequestScheduleExactAlarmsAuthorizationButton() {
    // TODO: onResume check scheduleExactAlarms state again
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
        if (isGranted) {
            onGranted()

        } else {
            goToSettings = true
        }
    }

    val requestPermission: () -> Unit = {
        val status = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        )

        when (PackageManager.PERMISSION_GRANTED) {
            status -> {
                onGranted()
            }

            else -> {
                // Display permission request
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DwTertiaryButton(
        text = if (goToSettings) "Grant permission in app settings" else "Request Permission",
        onClick = {
            if (goToSettings) {
                onOpenSettings()
            } else {
                requestPermission()
            }
        },
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
                alarmTriggerTime = TimeResult(0, 1, 0,0),
                alarmTriggerAtMillis = SystemClock.elapsedRealtime() + 3_661_000L,
                alarmTriggerAt = TimeResult(15, 30, 1,0),
            ),
            inActiveHours = false,
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
        ),
        onAction = {},
        onBackClicked = {},
    )
}
