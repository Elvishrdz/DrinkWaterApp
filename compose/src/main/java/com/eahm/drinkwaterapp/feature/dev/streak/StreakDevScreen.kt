package com.eahm.drinkwaterapp.feature.dev.streak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import kotlinx.datetime.LocalDate

@Composable
internal fun StreakDevScreen(
    state: StreakDevState.Data,
    onSelectedDateChanged: (LocalDate) -> Unit,
    onDrinkAmountChanged: (Float) -> Unit,
    onAddDrink: () -> Unit,
    onClearHistoryForDate: () -> Unit,
    onClearAllHistory: () -> Unit,
    onSetStoredBestGoalStreak: (Int) -> Unit,
    onSetStoredBestDrinkStreak: (Int) -> Unit,
    onClearStoredBests: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBackClicked, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Menu")
                }
            }

            // ── Live Streak Data ──────────────────────────────────────────────
            item {
                SectionHeader("Live Streak Data")
                DataRow("Current goal streak", "${state.currentGoalStreak} days")
                DataRow("Current drink streak", "${state.currentDrinkStreak} days")
                DataRow("Best goal streak (final)", "${state.bestGoalStreak} days")
                DataRow("Best drink streak (final)", "${state.bestDrinkStreak} days")
                DataRow("Personal best", "${state.personalBestMl} mL")
            }

            // ── DataStore Raw Values ──────────────────────────────────────────
            item {
                HorizontalDivider()
                SectionHeader("DataStore (persisted bests)")
                DataRow("Stored best goal streak", "${state.storedBestGoalStreak} days")
                DataRow("Stored best drink streak", "${state.storedBestDrinkStreak} days")
            }

            // ── Override Stored Bests ─────────────────────────────────────────
            item {
                HorizontalDivider()
                SectionHeader("Override Stored Bests")
                IntInputRow(
                    label = "Best goal streak (days)",
                    buttonLabel = "Set",
                    onSet = onSetStoredBestGoalStreak,
                )
                Spacer(Modifier.height(4.dp))
                IntInputRow(
                    label = "Best drink streak (days)",
                    buttonLabel = "Set",
                    onSet = onSetStoredBestDrinkStreak,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClearStoredBests,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset stored bests to 0")
                }
            }

            // ── Inject History ────────────────────────────────────────────────
            item {
                HorizontalDivider()
                SectionHeader("Inject Drink History")
                DateInputRow(
                    selected = state.selectedDate,
                    onDateChanged = onSelectedDateChanged,
                )
                Spacer(Modifier.height(4.dp))
                FloatInputRow(
                    value = state.drinkAmountInput,
                    label = "Amount (mL)",
                    buttonLabel = "Add drink",
                    onValueChanged = onDrinkAmountChanged,
                    onClick = onAddDrink,
                )
            }

            // ── Clear History ─────────────────────────────────────────────────
            item {
                HorizontalDivider()
                SectionHeader("Clear History")
                OutlinedButton(
                    onClick = onClearHistoryForDate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear history for ${state.selectedDate}")
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onClearAllHistory,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear ALL drink history")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = DwInk500, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun IntInputRow(
    label: String,
    buttonLabel: String,
    onSet: (Int) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { text.toIntOrNull()?.let { onSet(it) } },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun FloatInputRow(
    value: Float,
    label: String,
    buttonLabel: String,
    onValueChanged: (Float) -> Unit,
    onClick: () -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toInt().toString()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                text = input
                input.toFloatOrNull()?.let { onValueChanged(it) }
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Button(onClick = onClick, modifier = Modifier.padding(top = 8.dp)) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun DateInputRow(
    selected: LocalDate,
    onDateChanged: (LocalDate) -> Unit,
) {
    var yearText by remember(selected) { mutableStateOf(selected.year.toString()) }
    var monthText by remember(selected) { mutableStateOf((selected.month.ordinal + 1).toString()) }
    var dayText by remember(selected) { mutableStateOf(selected.day.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = yearText,
            onValueChange = {
                yearText = it; tryEmitDate(
                yearText,
                monthText,
                dayText,
                onDateChanged
            )
            },
            label = { Text("Year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(2f),
        )
        OutlinedTextField(
            value = monthText,
            onValueChange = {
                monthText = it; tryEmitDate(
                yearText,
                monthText,
                dayText,
                onDateChanged
            )
            },
            label = { Text("Mo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = dayText,
            onValueChange = {
                dayText = it; tryEmitDate(
                yearText,
                monthText,
                dayText,
                onDateChanged
            )
            },
            label = { Text("Day") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
    }
}

private fun tryEmitDate(
    year: String,
    month: String,
    day: String,
    onDateChanged: (LocalDate) -> Unit,
) {
    val y = year.toIntOrNull() ?: return
    val m = month.toIntOrNull()?.takeIf { it in 1..12 } ?: return
    val d = day.toIntOrNull()?.takeIf { it in 1..31 } ?: return
    runCatching { LocalDate(y, m, d) }.getOrNull()?.let { onDateChanged(it) }
}
