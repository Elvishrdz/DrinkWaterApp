package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.window.DialogProperties
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
 * Shown on first connection in the onboarding when the provider already has a backup. The user
 * chooses how to reconcile considering its the first time:
 *  - "Merge" → not required, no data should exist on the device at this point.
 *  - "Use remote backup" → download remote and overwrite this device's data.
 *  - "Upload this device" → not required, no data should exist on the device at this point.
 * Dismissing is not possible, user must choose after connecting the first time. If app is
 * restarted, we need to sync and ask again.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingFirstBackupSyncDialog(
    remote: ExistingRemoteBackup,
    onUseRemote: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        onDismissRequest = onDismiss
    ) {
        DialogContent(
            remote = remote,
            onUseRemote = onUseRemote,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun DialogContent(
    remote: ExistingRemoteBackup,
    onUseRemote: () -> Unit,
    onDismiss: () -> Unit,
) {
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

        // Freshness (last update) is the useful signal here; fall back to created_at for older
        // files that predate the last_updated_at field.
        val lastUpdatedValue = remote.metadata.lastUpdatedAt.takeIf { it > 0L }
            ?: remote.metadata.createdAt

        val lastUpdated = lastUpdatedValue
            .takeIf { it > 0L }
            ?.let {
                SimpleDateFormat(
                    "MMM d, yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(it))
            }

        val body = if (lastUpdated != null) {
            stringResource(R.string.backup_onb_initial_sync_body_dated, lastUpdated)
        } else {
            stringResource(R.string.backup_onb_initial_sync_body)
        }

        Text(
            text = body,
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            color = DwInk500,
        )

        Spacer(Modifier.height(14.dp))

        DwPrimaryButton(
            text = "Use remote backup",
            onClick = onUseRemote,
            modifier = Modifier.fillMaxWidth(),
        )

        DwSecondaryButton(
            text = "Just stay connected",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@DwPreview
@Composable
private fun DialogContentPreview() {
    DrinkWaterAppTheme {
        DialogContent(
            remote = ExistingRemoteBackup(
                BackupMetadata(
                    schemaVersion = 3,
                    createdAt = 1_700_000_000_000L,
                    lastUpdatedAt = 1_700_000_500_000L,
                ),
            ),
            onUseRemote = {},
            onDismiss = {},
        )
    }
}
