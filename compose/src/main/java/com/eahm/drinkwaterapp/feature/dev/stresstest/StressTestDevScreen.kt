package com.eahm.drinkwaterapp.feature.dev.stresstest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import kotlinx.datetime.LocalDate

@Composable
internal fun StressTestDevScreen(
    state: StressTestDevState.Data,
    onAnchorDateChanged: (LocalDate) -> Unit,
    onDaysChanged: (Int) -> Unit,
    onGoalModeChanged: (GoalMode) -> Unit,
    onDefaultGoalChanged: (Int) -> Unit,
    onMinGoalChanged: (Int) -> Unit,
    onMaxGoalChanged: (Int) -> Unit,
    onChangeEveryDaysChanged: (Int) -> Unit,
    onRangeStartChanged: (Int) -> Unit,
    onRangeEndChanged: (Int) -> Unit,
    onInsertGenerated: () -> Unit,
    onInsertPredefined: () -> Unit,
    onClearAll: () -> Unit,
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

            item {
                Text("Stress Test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Generates history + goal records backwards from the anchor date. " +
                            "The oldest day represents the app install date.",
                    fontSize = 12.sp,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Anchor date (most recent day)", fontWeight = FontWeight.SemiBold)
                DateInputRow(
                    selected = state.anchorDate,
                    onDateChanged = onAnchorDateChanged,
                )
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Number of days to populate", fontWeight = FontWeight.SemiBold)
                IntInputRow(label = "Days", value = state.days, onValueChanged = onDaysChanged)
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Goal mode", fontWeight = FontWeight.SemiBold)
                GoalModeSelector(selected = state.goalMode, onSelected = onGoalModeChanged)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.weight(1f)) {
                        IntInputRow("Default mL", state.defaultGoalMl, onDefaultGoalChanged)
                    }
                    Box(Modifier.weight(1f)) {
                        IntInputRow(
                            "Change every (days)",
                            state.changeEveryDays,
                            onChangeEveryDaysChanged
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.weight(1f)) {
                        IntInputRow(
                            "Min mL",
                            state.minGoalMl,
                            onMinGoalChanged
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        IntInputRow(
                            "Max mL",
                            state.maxGoalMl,
                            onMaxGoalChanged
                        )
                    }
                }
                if (state.goalMode == GoalMode.RANDOM_IN_RANGE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f)) {
                            IntInputRow(
                                "Range start (day)",
                                state.rangeStartDay,
                                onRangeStartChanged
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            IntInputRow("Range end (day)", state.rangeEndDay, onRangeEndChanged)
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                if (state.total > 0) {
                    Text("Progress: ${state.progress} / ${state.total}")
                }
                if (state.resultMessage.isNotEmpty()) {
                    Text(state.resultMessage, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onInsertGenerated,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Insert ${state.days} days")
                }
                Button(
                    onClick = onInsertPredefined,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Insert predefined dataset")
                }
                OutlinedButton(
                    onClick = onClearAll,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear all data")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun IntInputRow(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            input.trim().toIntOrNull()?.let { onValueChanged(it) }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun GoalModeSelector(
    selected: GoalMode,
    onSelected: (GoalMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GoalMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                label = { Text(mode.name) },
            )
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
                yearText = it
                tryEmitDate(
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
                monthText = it
                tryEmitDate(
                    yearText,
                    monthText,
                    dayText,
                    onDateChanged
                )
            },
            label = { Text("Month") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = dayText,
            onValueChange = {
                dayText = it
                tryEmitDate(
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

@Suppress("MagicNumber")
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
