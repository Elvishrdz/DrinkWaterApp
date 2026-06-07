package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

/**
 * Warning dialog shown on the drink (today) screen when tombstone records are approaching
 * their 15-day expiry and a sync has not yet occurred.
 *
 * Only shown when the user has cloud backup enabled and [tombstoneWarningPending] is true.
 */
@Composable
internal fun TombstoneSyncWarningDialog(
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.sync_tombstone_warning_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.sync_tombstone_warning_body),
                fontFamily = PlusJakartaSans,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onSyncNow,
                colors = ButtonDefaults.buttonColors(containerColor = DwMagenta),
            ) {
                Text(
                    text = stringResource(R.string.sync_tombstone_warning_sync),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.sync_tombstone_warning_dismiss),
                    fontFamily = PlusJakartaSans,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@DwPreview
@Composable
private fun TombstoneSyncWarningDialogPreview() {
    DrinkWaterAppTheme {
        TombstoneSyncWarningDialog(onSyncNow = {}, onDismiss = {})
    }
}
