package com.eahm.drinkwaterapp.feature.dev.dailygoal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.eahm.drinkwaterapp.core.database.entity.DailyGoalEntity
import kotlinx.datetime.LocalDate

@Composable
internal fun DailyGoalDevScreen(
    state: DailyGoalDevState.Data,
    onInputGoalChanged: (Float) -> Unit,
    onSetTodayGoal: () -> Unit,
    onSelectedDateChanged: (LocalDate) -> Unit,
    onSetGoalForSelectedDay: () -> Unit,
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
                Text(
                    "Today's goal: ${state.todayGoalMl} mL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Set today's goal", fontWeight = FontWeight.SemiBold)
                GoalInputRow(
                    value = state.inputGoalMl,
                    onValueChanged = onInputGoalChanged,
                    buttonLabel = "Set today's goal",
                    onClick = onSetTodayGoal,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Set goal for a specific past day", fontWeight = FontWeight.SemiBold)
                DateInputRow(
                    selected = state.selectedDate,
                    onDateChanged = onSelectedDateChanged,
                )
                GoalInputRow(
                    value = state.inputGoalMl,
                    onValueChanged = onInputGoalChanged,
                    buttonLabel = "Set goal for selected day",
                    onClick = onSetGoalForSelectedDay,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("All records (${state.allRecords.size})", fontWeight = FontWeight.SemiBold)
            }

            items(state.allRecords) { record ->
                GoalRecordRow(record)
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Clear all records")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun GoalInputRow(
    value: Float,
    onValueChanged: (Float) -> Unit,
    buttonLabel: String,
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
            label = { Text("mL") },
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
            label = { Text("Month") },
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
    onDateChanged: (LocalDate) -> Unit
) {
    val y = year.toIntOrNull() ?: return
    val m = month.toIntOrNull()?.takeIf { it in 1..12 } ?: return
    val d = day.toIntOrNull()?.takeIf { it in 1..31 } ?: return
    runCatching { LocalDate(y, m, d) }.getOrNull()?.let { onDateChanged(it) }
}

@Composable
private fun GoalRecordRow(record: DailyGoalEntity) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("Date: ${record.effectiveDate}  Goal: ${record.goalMl} mL")
        Text(
            text = "id: ...${record.id.takeLast(8)}",
            fontSize = 11.sp,
        )
    }
}
