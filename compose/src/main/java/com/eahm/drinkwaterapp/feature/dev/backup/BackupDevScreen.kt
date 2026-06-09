package com.eahm.drinkwaterapp.feature.dev.backup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.backup.domain.BackupSyncState
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

            // ── Controls: collapsible sections, scrollable, take the top half ──────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                var googleExpanded by rememberSaveable { mutableStateOf(true) }
                var dropboxExpanded by rememberSaveable { mutableStateOf(false) }
                var backgroundWorkExpanded by rememberSaveable { mutableStateOf(true) }
                var migrationExpanded by rememberSaveable { mutableStateOf(false) }
                var simulationExpanded by rememberSaveable { mutableStateOf(false) }
                var statsExpanded by rememberSaveable { mutableStateOf(false) }

                CollapsibleSection(
                    title = "Google Drive",
                    expanded = googleExpanded,
                    onToggle = { googleExpanded = !googleExpanded },
                ) {
                    DevProviderActions(
                        onConnect = { onAction(BackupDevAction.ConnectGoogle) },
                        onSync = { onAction(BackupDevAction.SyncGoogle) },
                        onRestore = { onAction(BackupDevAction.RestoreGoogle) },
                        onCheckStatus = { onAction(BackupDevAction.CheckStatusGoogle) },
                        onDisconnect = { onAction(BackupDevAction.DisconnectGoogle) },
                        onVisualize = { onAction(BackupDevAction.VisualizeGoogle) },
                        onDeleteFile = { onAction(BackupDevAction.DeleteFileGoogle) },
                    )
                }

                CollapsibleSection(
                    title = "Dropbox",
                    expanded = dropboxExpanded,
                    onToggle = { dropboxExpanded = !dropboxExpanded },
                ) {
                    DevProviderActions(
                        onConnect = { onAction(BackupDevAction.ConnectDropbox) },
                        onSync = { onAction(BackupDevAction.SyncDropbox) },
                        onRestore = { onAction(BackupDevAction.RestoreDropbox) },
                        onCheckStatus = { onAction(BackupDevAction.CheckStatusDropbox) },
                        onDisconnect = { onAction(BackupDevAction.DisconnectDropbox) },
                        onVisualize = { onAction(BackupDevAction.VisualizeDropbox) },
                        onDeleteFile = { onAction(BackupDevAction.DeleteFileDropbox) },
                    )
                }

                CollapsibleSection(
                    title = "Background Work",
                    expanded = backgroundWorkExpanded,
                    onToggle = { backgroundWorkExpanded = !backgroundWorkExpanded },
                ) {
                    BackgroundWorkControls(state = state, onAction = onAction)
                }

                CollapsibleSection(
                    title = "Migration",
                    expanded = migrationExpanded,
                    onToggle = { migrationExpanded = !migrationExpanded },
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onAction(BackupDevAction.MigrateGoogleToDropbox) }) {
                            Text("GDrive → Dropbox")
                        }
                        OutlinedButton(onClick = { onAction(BackupDevAction.MigrateDropboxToGoogle) }) {
                            Text("Dropbox → GDrive")
                        }
                    }
                }

                CollapsibleSection(
                    title = "Sync Simulation",
                    expanded = simulationExpanded,
                    onToggle = { simulationExpanded = !simulationExpanded },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

                CollapsibleSection(
                    title = "Merge Stats (this session)",
                    expanded = statsExpanded,
                    onToggle = { statsExpanded = !statsExpanded },
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
            }

            // ── Log: pinned at the bottom, always visible, scrolls internally ──────
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = logListState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
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

    state.backupPreview?.let { json ->
        BackupJsonViewerSheet(
            json = json,
            onClose = { onAction(BackupDevAction.DismissBackupPreview) },
        )
    }
}

/** Full-screen bottom sheet that shows a backup's formatted JSON for inspection. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupJsonViewerSheet(
    json: String,
    onClose: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Backup JSON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            SelectionContainer(modifier = Modifier.weight(1f)) {
                Text(
                    text = json,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    softWrap = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                )
            }

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Text("Close")
            }
        }
    }
}

/**
 * A titled section whose body collapses/expands when the header is tapped. Used to keep
 * the dev controls compact so the log panel below stays visible without scrolling.
 */
@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (expanded) "▾" else "▸",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (expanded) content()
        HorizontalDivider()
    }
}

@Composable
private fun BackgroundWorkControls(
    state: BackupDevState,
    onAction: (BackupDevAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Status: ${state.syncStatusLabel}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = state.workInfoSummary,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { onAction(BackupDevAction.RunBackgroundSyncNow) }) {
                Text("Run now")
            }
            OutlinedButton(onClick = { onAction(BackupDevAction.TriggerOnChangeSync) }) {
                Text("On-change")
            }
            OutlinedButton(onClick = { onAction(BackupDevAction.TriggerForegroundSync) }) {
                Text("Foreground")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { onAction(BackupDevAction.ScheduleDailySync) }) {
                Text("Schedule daily")
            }
            OutlinedButton(
                onClick = { onAction(BackupDevAction.CancelAllWork) },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Cancel all")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Status notification",
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = state.backupNotificationEnabled,
                onCheckedChange = { onAction(BackupDevAction.SetBackupNotification(it)) },
            )
        }
    }
}

@Composable
private fun DevProviderActions(
    onConnect: () -> Unit,
    onSync: () -> Unit,
    onRestore: () -> Unit,
    onCheckStatus: () -> Unit,
    onDisconnect: () -> Unit,
    onVisualize: () -> Unit,
    onDeleteFile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onConnect) { Text("Connect") }
            OutlinedButton(onClick = onSync) { Text("Sync") }
            OutlinedButton(onClick = onRestore) { Text("Restore") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onCheckStatus) { Text("Status") }
            OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
        }
        OutlinedButton(onClick = onVisualize) { Text("Visualize backup") }
        OutlinedButton(
            onClick = onDeleteFile,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Delete file (fresh install)")
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
