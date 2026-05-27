package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.eahm.drinkwaterapp.backup.CloudProviderType
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun BackupSettingsSection(
    state: BackupUiState,
    onConnectTap: () -> Unit,
    onSyncNowTap: () -> Unit,
    onDisconnectTap: () -> Unit,
) {
    BackupSection(title = stringResource(R.string.backup_section_title)) {
        BackupRow(
            icon = DwIcons.ShieldCheck,
            iconBg = DwTintWater,
            iconTint = DwWater,
            label = stringResource(R.string.backup_row_provider),
            onClick = if (state.isConnected) null else onConnectTap,
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
                onClick = if (state.isSyncing) null else onSyncNowTap,
            ) {
                BackupChevronTrailing()
            }

            HorizontalDivider(color = DwInk100)
            BackupRow(
                icon = DwIcons.Clock,
                iconBg = DwTintWater,
                iconTint = DwWater,
                label = stringResource(R.string.backup_row_last_sync),
            ) {
                val syncText = state.lastSyncAt
                    ?.let { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(it)) }
                    ?: stringResource(R.string.backup_last_sync_never)
                BackupValueTrailing(value = syncText, showChevron = false)
            }

            HorizontalDivider(color = DwInk100)
            BackupRow(
                icon = DwIcons.Close,
                iconBg = DwInk100,
                iconTint = DwInk500,
                label = stringResource(R.string.backup_row_disconnect),
                onClick = onDisconnectTap,
            ) {
                BackupChevronTrailing()
            }
        }
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
            color = DwInk500,
        )
        if (showChevron) {
            Icon(
                imageVector = DwIcons.ChevronRight,
                contentDescription = null,
                tint = DwInk400,
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

@DwPreview
@Composable
private fun BackupSettingsSectionPreview_Disconnected() {
    DrinkWaterAppTheme {
        BackupSettingsSection(
            state = BackupUiState(
                isConnected = false,
                providerType = null,
                lastSyncAt = null,
                isSyncing = false,
            ),
            onConnectTap = {},
            onSyncNowTap = {},
            onDisconnectTap = {},
        )
    }
}

@DwPreview
@Composable
private fun BackupSettingsSectionPreview_Connected() {
    DrinkWaterAppTheme {
        BackupSettingsSection(
            state = BackupUiState(
                isConnected = true,
                providerType = CloudProviderType.GOOGLE_DRIVE,
                lastSyncAt = System.currentTimeMillis(),
                isSyncing = false,
            ),
            onConnectTap = {},
            onSyncNowTap = {},
            onDisconnectTap = {},
        )
    }
}
