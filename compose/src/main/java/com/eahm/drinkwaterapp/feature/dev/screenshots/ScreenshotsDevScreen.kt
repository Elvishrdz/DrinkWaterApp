package com.eahm.drinkwaterapp.feature.dev.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ScreenshotsDevScreen(
    state: ScreenshotsDevState.Data,
    onVersionChanged: (String) -> Unit,
    onSetForScreenshots: () -> Unit,
    onInsertPreset: () -> Unit,
    onClearAll: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBackClicked, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Menu")
                }
            }

            item {
                Text("Screenshots", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Sets the app into a Play-Store-screenshot state (release-like): ads off, " +
                            "dev/reminders tabs hidden, Dropbox hidden, release version footer.",
                    fontSize = 12.sp,
                )
                Text(
                    "Disable by tapping the home progress ring 10 times.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Status", fontWeight = FontWeight.SemiBold)
                Text(
                    if (state.screenshotModeEnabled) "Screenshot mode: ON" else "Screenshot mode: OFF",
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Footer version (shown in screenshot mode)", fontWeight = FontWeight.SemiBold)
                var text by remember(state.version) { mutableStateOf(state.version) }
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onVersionChanged(it)
                    },
                    label = { Text("Version") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                if (state.resultMessage.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(state.resultMessage, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSetForScreenshots,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Set app for screenshots")
                }
                Button(
                    onClick = onInsertPreset,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Insert preset dataset (+ reminders)")
                }
                OutlinedButton(
                    onClick = onClearAll,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear all data")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
