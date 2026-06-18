package com.eahm.drinkwaterapp.feature.dev.age

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.eahm.drinkwaterapp.age.api.AgeSignals
import com.eahm.drinkwaterapp.age.api.AgeSimulatedResponse
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton

private const val YEAR_INPUT_LENGTH = 4

@Composable
internal fun AgeDevScreen(
    state: AgeDevState,
    onSetOverrideEnabled: (Boolean) -> Unit,
    onSelectResponse: (AgeSimulatedResponse) -> Unit,
    onRefresh: () -> Unit,
    onClearStored: () -> Unit,
    onSubmitBirthYear: (Int) -> Unit,
    onForceShowAgeScreen: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DwPrimaryButton(text = "Back to Menu", onClick = onBackClicked)

            SectionTitle("Current state")
            LabelValue("Stored age (DataStore)", state.storedSignal.describe())
            LabelValue("Live age (repository)", state.liveSignal.describe())
            LabelValue("Needs age collection", state.needsAgeCollection.toString())

            HorizontalDivider()
            SectionTitle("Override Play API (debug only)")
            OverrideSection(
                state = state,
                onSetOverrideEnabled = onSetOverrideEnabled,
                onSelectResponse = onSelectResponse,
            )
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Re-run resolution (refresh)") }

            HorizontalDivider()
            SectionTitle("Age screen & storage")
            Button(
                onClick = onForceShowAgeScreen,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Force-show age collection screen") }
            OutlinedButton(
                onClick = onClearStored,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Clear stored age") }

            HorizontalDivider()
            SectionTitle("Submit a declared birth year")
            BirthYearInput(onSubmit = onSubmitBirthYear)
        }
    }
}

@Composable
private fun OverrideSection(
    state: AgeDevState,
    onSetOverrideEnabled: (Boolean) -> Unit,
    onSelectResponse: (AgeSimulatedResponse) -> Unit,
) {
    if (!state.overrideAvailable) {
        Text("Override is disabled on release builds.")
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Use a fixed response (applied next startup)")
        Switch(checked = state.overrideEnabled, onCheckedChange = onSetOverrideEnabled)
    }

    if (state.overrideEnabled) {
        AgeSimulatedResponse.entries.forEach { response ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectResponse(response) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = state.selectedResponse == response,
                    onClick = { onSelectResponse(response) },
                )
                Text(response.label)
            }
        }
    }
}

@Composable
private fun BirthYearInput(onSubmit: (Int) -> Unit) {
    var input by remember { mutableStateOf("") }
    val year = input.toIntOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter(Char::isDigit).take(YEAR_INPUT_LENGTH) },
            label = { Text("Year of birth") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { year?.let(onSubmit) },
            enabled = year != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Submit birth year") }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold)
}

@Composable
private fun LabelValue(label: String, value: String) {
    Text(text = "$label: $value")
}

private fun AgeSignals?.describe(): String = this?.let {
    "treatment=${it.adTreatment}, source=${it.source}, status=${it.status}, " +
            "age=${it.ageLower}..${it.ageUpper}"
} ?: "Not set"
