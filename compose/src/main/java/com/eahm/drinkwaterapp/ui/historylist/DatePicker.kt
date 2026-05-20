package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import java.util.Calendar
import java.util.TimeZone

private val rowShape = RoundedCornerShape(14.dp)
private val iconShape = RoundedCornerShape(9.dp)

@Composable
fun DateTimePickerButton(
    modifier: Modifier = Modifier,
    selectedDateTime: LocalDateTime? = null,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    label: String = "Selecciona fecha",
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember(selectedDateTime) { mutableStateOf(selectedDateTime?.date) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, DwInk100, rowShape)
            .clickable { showDatePicker = true }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(iconShape)
                .background(DwTintWater),
        ) {
            Icon(
                imageVector = DwIcons.History,
                contentDescription = null,
                tint = DwWater,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text = selectedDate?.toFormattedString() ?: label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (selectedDate != null) MaterialTheme.colorScheme.onBackground else DwInk400,
            modifier = Modifier.weight(1f),
        )

        Icon(
            imageVector = DwIcons.ChevronRight,
            contentDescription = null,
            tint = DwInk400,
            modifier = Modifier.size(16.dp),
        )
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
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun LocalDate.toFormattedString(): String {
    val months = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${months[monthNumber - 1]} $dayOfMonth, $year"
}
