package com.eahm.drinkwaterapp.feature.settings.age

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSource
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.age.AgeBracketOption
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AgeSettingsSection(
    viewModel: AgeSettingsViewModel = koinViewModel(),
) {
    val signals by viewModel.ageSignals.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // API-sourced values are authoritative; only declared/unset values are editable.
    val editable = signals?.source != AgeSource.API

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.age_settings_section_title).uppercase(),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = editable) { showDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = DwIcons.Flag,
                    contentDescription = null,
                    tint = DwWater,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.age_settings_row_label),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = signals.sourceLabel(),
                        fontSize = 12.sp,
                        color = DwInk400,
                    )
                }
                Text(
                    text = signals.rangeLabel(),
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = DwInk500,
                )
                if (editable) {
                    Icon(
                        imageVector = DwIcons.ChevronRight,
                        contentDescription = null,
                        tint = DwInk400,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }

    if (showDialog) {
        AgeOptionsDialog(
            current = AgeBracketOption.fromTreatment(signals?.adTreatment),
            onDismiss = { showDialog = false },
            onConfirm = { option ->
                showDialog = false
                viewModel.submitSelection(option)
            },
        )
    }
}

@Composable
private fun AgeOptionsDialog(
    current: AgeBracketOption?,
    onDismiss: () -> Unit,
    onConfirm: (AgeBracketOption) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.age_collection_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.age_settings_dialog_description),
                    color = DwInk400,
                    fontSize = 13.sp,
                )
                AgeBracketOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = option },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option },
                        )
                        Text(stringResource(option.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = { selected?.let(onConfirm) },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun AgeSignals?.rangeLabel(): String = stringResource(
    AgeBracketOption.fromTreatment(this?.adTreatment)?.labelRes ?: R.string.age_range_not_set,
)

@Composable
private fun AgeSignals?.sourceLabel(): String = stringResource(
    when (this?.source) {
        AgeSource.API -> R.string.age_settings_source_play
        AgeSource.DECLARED -> R.string.age_settings_source_declared
        null -> R.string.age_settings_source_unset
    },
)
