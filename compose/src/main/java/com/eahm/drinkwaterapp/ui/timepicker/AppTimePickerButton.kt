package com.eahm.drinkwaterapp.ui.timepicker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerButton(
    text: String,
    time: TimeSelected = TimeSelected(0, 0),
    onTimeSelected: (TimeSelected) -> Unit,
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showModal = !showModal },
    ) {
        Text(text)
    }

    if (showModal) {
        AppTimePickerDialog(
            time = time,
            onTimeSelected = onTimeSelected,
            onDismiss = { showModal = false },
        )
    }
}

@Preview
@Composable
@Suppress("AssignedValueIsNeverRead")
private fun AppTimePickerButtonPreview() {
    var selected by remember { mutableStateOf(TimeSelected(10, 20)) }

    AppTimePickerButton(
        text = "Time",
        time = selected,
        onTimeSelected = { selected = it },
    )
}
