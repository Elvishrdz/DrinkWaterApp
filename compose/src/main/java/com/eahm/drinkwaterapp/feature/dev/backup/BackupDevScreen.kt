package com.eahm.drinkwaterapp.feature.dev.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.backup.BackupSyncState
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

@Composable
internal fun BackupDevScreen(
    state: BackupDevState,
    onAction: (BackupDevAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    val logListState = rememberLazyListState()

    LaunchedEffect(state.log.size) {
        if (state.log.isNotEmpty()) {
            logListState.animateScrollToItem(state.log.size - 1)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onBackClicked) { Text("← Menu") }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Sync: ${state.syncState::class.simpleName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (state.syncState) {
                        is BackupSyncState.Error -> MaterialTheme.colorScheme.error
                        is BackupSyncState.Success -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = logListState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Google Drive section
                item {
                    DevProviderSection(
                        title = "Google Drive",
                        onConnect = { onAction(BackupDevAction.ConnectGoogle) },
                        onSync = { onAction(BackupDevAction.SyncGoogle) },
                        onRestore = { onAction(BackupDevAction.RestoreGoogle) },
                        onCheckStatus = { onAction(BackupDevAction.CheckStatusGoogle) },
                        onDisconnect = { onAction(BackupDevAction.DisconnectGoogle) },
                    )
                }

                // Dropbox section
                item {
                    DevProviderSection(
                        title = "Dropbox",
                        onConnect = { onAction(BackupDevAction.ConnectDropbox) },
                        onSync = { onAction(BackupDevAction.SyncDropbox) },
                        onRestore = { onAction(BackupDevAction.RestoreDropbox) },
                        onCheckStatus = { onAction(BackupDevAction.CheckStatusDropbox) },
                        onDisconnect = { onAction(BackupDevAction.DisconnectDropbox) },
                    )
                }

                // Migration section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Migration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onAction(BackupDevAction.MigrateGoogleToDropbox) }) {
                                Text("GDrive → Dropbox")
                            }
                            OutlinedButton(onClick = { onAction(BackupDevAction.MigrateDropboxToGoogle) }) {
                                Text("Dropbox → GDrive")
                            }
                        }
                    }
                }

                // Sync simulation section
                item {
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Sync Simulation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onAction(BackupDevAction.SimulateGoalConflict) }) {
                                Text("Goal conflict")
                            }
                            OutlinedButton(onClick = { onAction(BackupDevAction.SimulatePrefsConflict) }) {
                                Text("Prefs conflict")
                            }
                        }
                        OutlinedButton(onClick = { onAction(BackupDevAction.SimulateTombstoneWarning) }) {
                            Text("Tombstone warning")
                        }
                    }
                }

                // Merge stats
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Merge Stats (this session)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Records merged: ${state.mergedRecordCount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = "Tombstones applied: ${state.tombstonesApplied}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = "Auto-resolved conflicts: ${state.autoResolvedConflicts}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                // Log header
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Log (${state.log.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        OutlinedButton(onClick = { onAction(BackupDevAction.ClearLog) }) {
                            Text("Clear")
                        }
                    }
                }

                // Log entries
                items(state.log) { entry ->
                    Text(
                        text = entry,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun DevProviderSection(
    title: String,
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onRestore: () -> Unit,
    onCheckStatus: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedButton(onClick = onConnect) { Text("Connect") }
            OutlinedButton(onClick = onSync) { Text("Sync") }
            OutlinedButton(onClick = onRestore) { Text("Restore") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedButton(onClick = onCheckStatus) { Text("Status") }
            OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupDevScreenPreview() {
    DrinkWaterAppTheme {
        BackupDevScreen(
            state = BackupDevState(
                syncState = BackupSyncState.Idle,
                log = listOf(
                    "Starting app...",
                    "Checking Google Drive status...",
                    "Google Drive connected.",
                    "Syncing data...",
                    "Sync successful."
                )
            ),
            onAction = {},
            onBackClicked = {}
        )
    }
}
