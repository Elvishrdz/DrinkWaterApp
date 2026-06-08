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
import com.eahm.drinkwaterapp.backup.domain.model.CorruptedBackup
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWarning
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

/**
 * Destructive, irreversible decision shown when the remote backup cannot be recognized.
 * Overwriting replaces the corrupted remote file with this device's data; the previous
 * remote contents cannot be recovered afterwards. Declining leaves the remote untouched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CorruptedBackupDialog(
    corruption: CorruptedBackup,
    onOverwrite: () -> Unit,
    onLeave: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onLeave) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.backup_corrupted_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val bodyRes = when (corruption.reason) {
                CorruptedBackup.Reason.UNREADABLE -> R.string.backup_corrupted_body_unreadable
                CorruptedBackup.Reason.UNRECOGNIZED_STRUCTURE ->
                    R.string.backup_corrupted_body_unrecognized
            }
            Text(
                text = stringResource(bodyRes),
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                color = DwInk500,
            )

            Text(
                text = stringResource(R.string.backup_corrupted_irreversible_warning),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DwWarning,
            )

            DwPrimaryButton(
                text = stringResource(R.string.backup_corrupted_overwrite),
                onClick = onOverwrite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )
            DwSecondaryButton(
                text = stringResource(R.string.backup_corrupted_leave),
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@DwPreview
@Composable
private fun CorruptedBackupDialogPreview() {
    DrinkWaterAppTheme {
        CorruptedBackupDialog(
            corruption = CorruptedBackup(CorruptedBackup.Reason.UNRECOGNIZED_STRUCTURE),
            onOverwrite = {},
            onLeave = {},
        )
    }
}
