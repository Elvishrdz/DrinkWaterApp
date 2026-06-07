package com.eahm.drinkwaterapp.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.backup.GoalRecord
import com.eahm.drinkwaterapp.backup.PreferencesRecord
import com.eahm.drinkwaterapp.backup.SyncConflict
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import kotlinx.datetime.LocalDate

/**
 * Bottom sheet shown when the sync engine detects a conflict that requires user resolution.
 * Handles both [SyncConflict.GoalConflict] and [SyncConflict.PreferencesConflict].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SyncConflictSheet(
    conflict: SyncConflict,
    onKeepLocal: () -> Unit,
    onUseRemote: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onKeepLocal,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (conflict) {
                is SyncConflict.GoalConflict -> GoalConflictContent(conflict)
                is SyncConflict.PreferencesConflict -> PrefsConflictContent(conflict)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onUseRemote,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DwMagenta),
            ) {
                Text(
                    text = stringResource(R.string.sync_conflict_use_remote),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedButton(
                onClick = onKeepLocal,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, DwInk100),
            ) {
                Text(
                    text = stringResource(R.string.sync_conflict_keep_local),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun GoalConflictContent(conflict: SyncConflict.GoalConflict) {
    val date = LocalDate.fromEpochDays(conflict.effectiveDate)
    Text(
        text = stringResource(R.string.sync_goal_conflict_title),
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.sync_goal_conflict_body, date.toString()),
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConflictOptionCard(
            label = stringResource(R.string.sync_conflict_local_label),
            value = stringResource(R.string.sync_conflict_goal_value, conflict.localGoal.goalMl.toInt()),
            modifier = Modifier.weight(1f),
        )
        ConflictOptionCard(
            label = stringResource(R.string.sync_conflict_remote_label),
            value = stringResource(R.string.sync_conflict_goal_value, conflict.remoteGoal.goalMl.toInt()),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrefsConflictContent(conflict: SyncConflict.PreferencesConflict) {
    Text(
        text = stringResource(R.string.sync_prefs_conflict_title),
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.sync_prefs_conflict_body),
        fontFamily = PlusJakartaSans,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConflictOptionCard(
            label = stringResource(R.string.sync_conflict_local_label),
            value = prefsDescription(conflict.localPrefs),
            modifier = Modifier.weight(1f),
        )
        ConflictOptionCard(
            label = stringResource(R.string.sync_conflict_remote_label),
            value = prefsDescription(conflict.remotePrefs),
            modifier = Modifier.weight(1f),
        )
    }
}

private fun prefsDescription(prefs: PreferencesRecord): String =
    "${prefs.unitPreference} · ${prefs.timeFormat} · ${prefs.amountPerDrink.toInt()} ml"

@Composable
private fun ConflictOptionCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(DwTintMagenta, RoundedCornerShape(12.dp))
            .border(1.dp, DwMagenta.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = DwMagenta,
        )
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DwPreview
@Composable
private fun SyncConflictSheetPreview_Goal() {
    DrinkWaterAppTheme {
        SyncConflictSheet(
            conflict = SyncConflict.GoalConflict(
                effectiveDate = 0,
                localGoal = GoalRecord(id = "a", effectiveDate = 0, goalMl = 2000f),
                remoteGoal = GoalRecord(id = "b", effectiveDate = 0, goalMl = 2500f),
            ),
            onKeepLocal = {},
            onUseRemote = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DwPreview
@Composable
private fun SyncConflictSheetPreview_Prefs() {
    DrinkWaterAppTheme {
        SyncConflictSheet(
            conflict = SyncConflict.PreferencesConflict(
                localPrefs = PreferencesRecord(unitPreference = "ML", timeFormat = "24h", amountPerDrink = 250f),
                remotePrefs = PreferencesRecord(unitPreference = "OZ", timeFormat = "12h", amountPerDrink = 300f),
            ),
            onKeepLocal = {},
            onUseRemote = {},
        )
    }
}
