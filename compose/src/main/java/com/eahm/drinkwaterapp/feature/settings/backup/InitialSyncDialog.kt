package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.backup.domain.model.BackupMetadata
import com.eahm.drinkwaterapp.backup.domain.model.ExistingRemoteBackup
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shown on first connection when the provider already has a backup. The user chooses how
 * to reconcile:
 *  - "Merge" → combine local + remote with the normal sync logic (non-destructive).
 *  - "Use remote backup" → download remote and overwrite this device's data.
 *  - "Upload this device" → overwrite the remote backup with this device's data.
 * Dismissing leaves both sides untouched; the user stays connected and can sync later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InitialSyncDialog(
    remote: ExistingRemoteBackup,
    onMerge: () -> Unit,
    onUseRemote: () -> Unit,
    onUploadLocal: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_initial_sync_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val createdAt = remote.metadata.createdAt.takeIf { it > 0L }?.let {
                SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(it))
            }
            val body = if (createdAt != null) {
                stringResource(R.string.backup_initial_sync_body_dated, createdAt)
            } else {
                stringResource(R.string.backup_initial_sync_body)
            }
            Text(
                text = body,
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                color = DwInk500,
            )

            DwPrimaryButton(
                text = stringResource(R.string.backup_initial_sync_merge),
                onClick = onMerge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )
            DwSecondaryButton(
                text = stringResource(R.string.backup_initial_sync_use_remote),
                onClick = onUseRemote,
                modifier = Modifier.fillMaxWidth(),
            )
            DwSecondaryButton(
                text = stringResource(R.string.backup_initial_sync_upload_local),
                onClick = onUploadLocal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@DwPreview
@Composable
private fun InitialSyncDialogPreview() {
    DrinkWaterAppTheme {
        InitialSyncDialog(
            remote = ExistingRemoteBackup(
                BackupMetadata(
                    schemaVersion = 2,
                    appVersion = "1.0.0",
                    createdAt = 1_700_000_000_000L,
                    deviceId = "pixel-8",
                ),
            ),
            onMerge = {},
            onUseRemote = {},
            onUploadLocal = {},
            onDismiss = {},
        )
    }
}
