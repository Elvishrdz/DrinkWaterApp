package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import java.util.Calendar
import java.util.TimeZone

@Composable
fun DateTimePickerButton(
    modifier: Modifier = Modifier,
    selectedDateTime: LocalDateTime? = null,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    label: String = "Selecciona fecha",
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(selectedDateTime?.date) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(com.eahm.drinkwaterapp.R.drawable.ic_drink_holo_dark),
                contentDescription = "Seleccionar fecha",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = selectedDate?.toString() ?: label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                val time = selectedDateTime?.time ?: kotlinx.datetime.LocalTime(0, 0, 0)
                onDateTimeSelected(date.atTime(time))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDays()?.times(86400000L)?.plus(
            TimeZone.getDefault().getOffset(System.currentTimeMillis())
        )
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = millis
                    }
                    val date = LocalDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    onDateSelected(date)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}