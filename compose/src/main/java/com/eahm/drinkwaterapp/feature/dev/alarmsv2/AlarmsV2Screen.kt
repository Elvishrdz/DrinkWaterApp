package com.eahm.drinkwaterapp.feature.dev.alarmsv2

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmsV2Screen(
    state: AlarmsV2State,
    onAction: (AlarmsV2Action) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms V2 — Dual Alarm System") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) { Text("← Back") }
                },
            )
        },
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
        // Clock
        Text(
            text = state.currentTime,
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )

        HorizontalDivider()

        // Alarm statuses
        SectionTitle("Alarm Status")
        StatusRow(label = "Exact alarm active", value = state.exactAlarmActive)
        StatusRow(label = "Inexact alarm active", value = state.inexactAlarmActive)

        HorizontalDivider()

        // Permission & active hours
        SectionTitle("System State")
        StatusRow(label = "Exact permission granted", value = state.exactPermissionGranted)
        StatusRow(label = "Within active hours (06:00–18:00)", value = state.isWithinActiveHours)
        InfoRow(label = "Next slot", value = state.nextSlot)

        HorizontalDivider()

        // Actions
        SectionTitle("Actions")

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
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction(AlarmsV2Action.OpenExactPermissionSettings) },
            ) {
                Text("Open Exact Alarm Permission Settings")
            }
        }
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
