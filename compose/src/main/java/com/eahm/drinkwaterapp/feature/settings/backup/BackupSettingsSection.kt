package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.backup.domain.model.CloudProviderType
import com.eahm.drinkwaterapp.backup.domain.model.InitialSyncDecision
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWarning
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("CyclomaticComplexMethod", "CognitiveComplexMethod")
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun BackupSettingsSection(
    state: BackupUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSyncNow: () -> Unit,
    onRestore: () -> Unit = {},
    onSyncOnSaveToggled: (Boolean) -> Unit = {},
    onResolveConflict: (useRemote: Boolean) -> Unit = {},
    onResolveCorruption: (overwrite: Boolean) -> Unit = {},
    onResolveInitialSync: (InitialSyncDecision) -> Unit = {},
    onDismissInitialSync: () -> Unit = {},
    onAutoBackupHourChanged: (Int) -> Unit = {},
) {
    var showHourPicker by remember { mutableStateOf(false) }
    if (showHourPicker) {
        BackupHourPickerDialog(
            selectedHour = state.autoBackupHour,
            onHourSelected = {
                onAutoBackupHourChanged(it)
                showHourPicker = false
            },
            onDismiss = { showHourPicker = false },
        )
    }
    state.pendingConflict?.let { conflict ->
        SyncConflictSheet(
            conflict = conflict,
            onKeepLocal = { onResolveConflict(false) },
            onUseRemote = { onResolveConflict(true) },
        )
    }

    state.pendingCorruption?.let { corruption ->
        CorruptedBackupDialog(
            corruption = corruption,
            onOverwrite = { onResolveCorruption(true) },
            onLeave = { onResolveCorruption(false) },
        )
    }

    state.pendingInitialSync?.let { remote ->
        InitialSyncDialog(
            remote = remote,
            onMerge = { onResolveInitialSync(InitialSyncDecision.MERGE) },
            onUseRemote = { onResolveInitialSync(InitialSyncDecision.USE_REMOTE) },
            onUploadLocal = { onResolveInitialSync(InitialSyncDecision.UPLOAD_LOCAL) },
            onDismiss = onDismissInitialSync,
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BackupSection(title = stringResource(R.string.backup_section_title)) {
            state.error?.let { error ->
                val errorMessage = when (error) {
                    BackupUIError.NOT_SIGNED_IN -> stringResource(R.string.backup_error_not_signed_in)
                    BackupUIError.UNKNOWN -> stringResource(R.string.backup_error_unknown)
                }

                BackupErrorRow(errorMessage = errorMessage)
                HorizontalDivider(color = DwInk100)
            }

            backupStatusMessage(state.statusMessage)?.let { message ->
                BackupErrorRow(errorMessage = message)
                HorizontalDivider(color = DwInk100)
            }

            BackupRow(
                icon = DwIcons.ShieldCheck,
                iconBg = DwTintWater,
                iconTint = DwWater,
                label = stringResource(R.string.backup_row_provider),
                onClick = if (state.isConnected) null else onConnect,
            ) {
                val statusText = when {
                    state.isSyncing -> stringResource(R.string.backup_status_syncing)
                    state.isConnected -> state.providerType?.displayName
                        ?: stringResource(R.string.backup_status_not_connected)

                    else -> stringResource(R.string.backup_status_not_connected)
                }
                BackupValueTrailing(value = statusText, showChevron = !state.isConnected)
            }

            if (state.isConnected) {
                HorizontalDivider(color = DwInk100)
                BackupRow(
                    icon = DwIcons.Repeat,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = stringResource(R.string.backup_row_sync_now),
                    onClick = if (state.isSyncing) null else onSyncNow,
                ) {
                    BackupChevronTrailing()
                }

                HorizontalDivider(color = DwInk100)
                BackupRow(
                    icon = DwIcons.History,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = stringResource(R.string.backup_row_restore),
                    onClick = if (state.isSyncing) null else onRestore,
                ) {
                    BackupChevronTrailing()
                }

                HorizontalDivider(color = DwInk100)
                BackupRow(
                    icon = DwIcons.History,
                    iconBg = DwTintWater,
                    iconTint = DwWater,
                    label = stringResource(R.string.backup_row_auto_hour),
                    onClick = if (state.isSyncing) null else fun() { showHourPicker = true },
                ) {
                    BackupValueTrailing(
                        value = stringResource(
                            R.string.backup_auto_hour_value,
                            state.autoBackupHour,
                        ),
                    )
                }

                HorizontalDivider(color = DwInk100)
                BackupRow(
                    icon = DwIcons.Repeat,
                    iconBg = DwTintMagenta,
                    iconTint = DwMagenta,
                    label = stringResource(R.string.sync_on_save_label),
                ) {
                    Switch(
                        checked = state.syncOnSaveEnabled,
                        onCheckedChange = onSyncOnSaveToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DwMagenta,
                            checkedTrackColor = DwTintMagenta,
                        ),
                    )
                }

                HorizontalDivider(color = DwInk100)
                BackupRow(
                    icon = DwIcons.Close,
                    iconBg = DwInk100,
                    iconTint = DwInk500,
                    label = stringResource(R.string.backup_row_disconnect),
                    onClick = if (state.isSyncing) null else onDisconnect,
                ) {
                    BackupChevronTrailing()
                }
            }
        }

        if (state.isConnected) {
            SyncPendingBanner(visible = state.syncPending)

            LinkedDevicesSection(devices = state.seenDevices)

            BackupInfoFooter(
                lastSyncAt = state.lastSyncAt,
                syncOnSaveEnabled = state.syncOnSaveEnabled,
            )
        }
    }
}

@Composable
private fun BackupErrorRow(errorMessage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DwWarning.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = DwIcons.Warning,
                contentDescription = null,
                tint = DwWarning,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = DwInk500,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Informative footer shown below the backup rows: last backup time + backup-mode hint. */
@Composable
private fun BackupInfoFooter(lastSyncAt: Long?, syncOnSaveEnabled: Boolean) {
    val lastBackup = lastSyncAt
        ?.let { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(it)) }
        ?: stringResource(R.string.backup_last_sync_never)
    // The hint mirrors the Sync-on-save toggle so it never contradicts the actual mode.
    val hint = if (syncOnSaveEnabled) {
        stringResource(R.string.backup_info_auto)
    } else {
        stringResource(R.string.backup_info_manual)
    }
    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "${stringResource(R.string.backup_row_last_sync)} · $lastBackup",
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = hint,
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackupSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = TextUnit(0.10f, TextUnitType.Em),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(modifier = Modifier.outlineBorderStyle()) {
            content()
        }
    }
}

@Composable
private fun BackupRow(
    icon: ImageVector,
    iconBg: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .then(clickModifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun BackupValueTrailing(value: String, showChevron: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showChevron) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun BackupChevronTrailing() {
    Icon(
        imageVector = DwIcons.ChevronRight,
        contentDescription = null,
        tint = DwInk400,
        modifier = Modifier.size(16.dp),
    )
}

/** Maps a [BackupStatusMessage] to user-facing copy. Returns null when there's nothing to show. */
@Composable
private fun backupStatusMessage(message: BackupStatusMessage): String? = when (message) {
    BackupStatusMessage.NONE,
    BackupStatusMessage.SYNCING -> null // SYNCING is reflected in the provider row instead.
    BackupStatusMessage.WAITING_FOR_CONNECTION ->
        stringResource(R.string.backup_status_waiting_connection)

    BackupStatusMessage.WILL_RETRY -> stringResource(R.string.backup_status_will_retry)
    BackupStatusMessage.ON_SAVE_RETRY -> stringResource(R.string.backup_status_on_save_retry)
    BackupStatusMessage.FAILED_USE_MANUAL -> stringResource(R.string.backup_status_failed_manual)
}

@Composable
private fun BackupHourPickerDialog(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.backup_row_auto_hour)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = HOUR_PICKER_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState())
            ) {
                for (hour in 0 until HOURS_IN_DAY) {
                    Text(
                        text = stringResource(R.string.backup_auto_hour_value, hour),
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (hour == selectedHour) FontWeight.Bold else FontWeight.Normal,
                        color = if (hour == selectedHour) DwMagenta else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHourSelected(hour) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.backup_hour_picker_cancel))
            }
        },
    )
}

private const val HOURS_IN_DAY = 24
private val HOUR_PICKER_MAX_HEIGHT = 280.dp

@DwPreview
@Composable
private fun DisconnectedPreview() {
    DrinkWaterAppTheme {
        BackupSettingsSection(
            state = BackupUiState(
                isConnected = false,
                providerType = null,
                lastSyncAt = null,
                isSyncing = false,
            ),
            onConnect = {},
            onDisconnect = {},
            onSyncNow = {},
        )
    }
}

@DwPreview
@Composable
private fun ConnectedPreview() {
    DrinkWaterAppTheme {
        BackupSettingsSection(
            state = BackupUiState(
                isConnected = true,
                providerType = CloudProviderType.GOOGLE_DRIVE,
                lastSyncAt = System.currentTimeMillis(),
                isSyncing = false,
            ),
            onConnect = {},
            onDisconnect = {},
            onSyncNow = {},
        )
    }
}
