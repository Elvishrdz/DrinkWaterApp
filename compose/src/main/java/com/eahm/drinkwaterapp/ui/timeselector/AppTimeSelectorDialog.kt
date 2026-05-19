package com.eahm.drinkwaterapp.ui.timeselector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.isPm
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eahm.drinkwaterapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimeSelectorDialog(
    time: TimeSelected? = null,
    is24HourFormat: Boolean = rememberTimePickerState().is24hour,
    is24HourResult: Boolean = true,
    onTimeSelected: (TimeSelected) -> Unit,
    onDismiss: () -> Unit,
    canChangeTimeFormat: Boolean = true,
    message: String? = null,
    debug: Boolean = false,
) {
    var is24Hour by rememberSaveable { mutableStateOf(is24HourFormat) }

    // using if-else to recompose and change ui
    val timeState = if (is24Hour) {
        rememberTimePickerState(
            is24Hour = true,
            initialHour = time?.hour ?: 0,
            initialMinute = time?.minute ?: 0,
        )

    } else {
        rememberTimePickerState(
            is24Hour = false,
            initialHour = time?.hour ?: 0,
            initialMinute = time?.minute ?: 0,
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        ),
    ) {
        ElevatedCard(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (debug) {
                    Column {
                        Text(text = "is24HourFormat: $is24HourFormat")
                        Text(text = "is24Hour: $is24Hour")
                        Text(text = "is24Hour (timeState): ${timeState.is24hour}")
                    }
                }

                if (message != null) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        text = message,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = when (timeState.selection) {
                        TimePickerSelectionMode.Hour -> stringResource(R.string.time_selector_title_hour)
                        TimePickerSelectionMode.Minute -> stringResource(R.string.time_selector_title_minute)
                        else -> stringResource(R.string.time_selector_title_time)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                TimePicker(
                    state = timeState,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (canChangeTimeFormat) {
                        OutlinedButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                is24Hour = !is24Hour
                                timeState.is24hour = is24Hour
                            }
                        ) {
                            Text(
                                text = if (is24Hour) {
                                    stringResource(R.string.time_selector_format_ampm)
                                } else {
                                    stringResource(R.string.time_selector_format_24h)
                                },
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = onDismiss,
                    ) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            val hour = if (!is24HourResult &&
                                !timeState.is24hour &&
                                timeState.isPm
                            ) {
                                timeState.hour + 12
                            } else {
                                timeState.hour
                            }

                            onTimeSelected(
                                TimeSelected(
                                    hour = hour,
                                    minute = timeState.minute,
                                )
                            )

                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(R.string.action_ok))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
@Suppress("AssignedValueIsNeverRead")
private fun AppTimeSelectorDialogPreview() {
    var selected by remember { mutableStateOf(TimeSelected(10, 20)) }

    AppTimeSelectorDialog(
        time = selected,
        onTimeSelected = { selected = it },
        onDismiss = {},
    )
}