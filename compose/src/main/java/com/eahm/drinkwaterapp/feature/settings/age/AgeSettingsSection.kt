package com.eahm.drinkwaterapp.feature.settings.age

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.age.api.AdTreatment
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSource
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

private const val YEAR_INPUT_LENGTH = 4
private const val MAX_PLAUSIBLE_AGE = 120

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
            text = "AGE & ADS",
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
                        text = "Age range (for ad suitability)",
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
        BirthYearDialog(
            onDismiss = { showDialog = false },
            onConfirm = { year ->
                showDialog = false
                viewModel.submitBirthYear(year)
            },
        )
    }
}

@Composable
private fun BirthYearDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var input by remember { mutableStateOf("") }
    val year = input.toIntOrNull()
    val valid = year != null && (currentYear - year) in 0..MAX_PLAUSIBLE_AGE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your year of birth") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Used only to keep ads appropriate for your age and to comply with " +
                        "the law. Never shared, profiled, or used to target ads.",
                    color = DwInk400,
                    fontSize = 13.sp,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter(Char::isDigit).take(YEAR_INPUT_LENGTH) },
                    label = { Text("Year of birth") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { year?.let(onConfirm) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun AgeSignals?.rangeLabel(): String = when (this?.adTreatment) {
    AdTreatment.CHILD_DIRECTED -> "Under 13"
    AdTreatment.UNDER_AGE_OF_CONSENT -> "13–17"
    AdTreatment.STANDARD -> "18 or older"
    null -> "Not set"
}

private fun AgeSignals?.sourceLabel(): String = when (this?.source) {
    AgeSource.API -> "Set by Google Play"
    AgeSource.DECLARED -> "You set this · tap to change"
    null -> "Tap to set"
}
