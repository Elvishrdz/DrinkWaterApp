package com.eahm.drinkwaterapp.ui.timeselector

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimeSelectorButton(
    text: String,
    time: TimeSelected? = null,
    is24HourFormat: Boolean = rememberTimePickerState().is24hour,
    onTimeSelected: (TimeSelected) -> Unit,
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showModal = !showModal },
    ) {
        Text(text)
    }

    if (showModal) {
        AppTimeSelectorDialog(
            time = time,
            is24HourFormat = is24HourFormat,
            onTimeSelected = onTimeSelected,
            onDismiss = { showModal = false },
        )
    }
}

@Preview
@Composable
@Suppress("AssignedValueIsNeverRead")
private fun AppTimeSelectorButtonPreview() {
    var selected by remember { mutableStateOf(TimeSelected(10, 20)) }

    AppTimeSelectorButton(
        text = "Time",
        time = selected,
        onTimeSelected = { selected = it },
    )
}
