package com.eahm.drinkwaterapp.ui.timeselector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimeSelector(
    time: TimeSelected? = null,
    is24HourFormat: Boolean = rememberTimePickerState().is24hour,
    onTimeSelected: (TimeSelected) -> Unit,
    content: @Composable (() -> Unit) -> Unit,
) {
    var showModal by rememberSaveable { mutableStateOf(false) }

    content {
        showModal = !showModal
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
private fun AppTimeSelectorPreview() {
    var selected by remember { mutableStateOf(TimeSelected(10, 20)) }

    AppTimeSelector(
        time = selected,
        onTimeSelected = { selected = it },
        content = { toggle ->
            Column {
                Text(
                    modifier = Modifier.padding(7.dp),
                    text = "Demo"
                )

                Card(
                    onClick = toggle,
                ) {
                    Text(
                        modifier = Modifier.padding(7.dp),
                        text = "Choose"
                    )
                }
            }
        },
    )
}
