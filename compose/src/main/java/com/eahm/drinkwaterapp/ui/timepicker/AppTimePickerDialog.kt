package com.eahm.drinkwaterapp.ui.timepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    time: TimeSelected = TimeSelected(0, 0),
    onDismiss: () -> Unit,
    onTimeSelected: (TimeSelected) -> Unit,
) {
    var hour by rememberSaveable { mutableStateOf(time.hour) }
    var minute by rememberSaveable { mutableStateOf(time.minute) }

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
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = "Set time",
                )

                PickHourMinute(
                    initialHour = hour,
                    onHourChange = { hour = it },
                    initialMinute = minute,
                    onMinuteChange = { minute = it },
                    containerColor = Color.Transparent,
                    focusIndicator = PickTimeFocusIndicator(
                        enabled = true,
                        widthFull = false,
                        background = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                        ),
                    ),
                    selectedTextStyle = PickTimeTextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                    ),
                    unselectedTextStyle = PickTimeTextStyle(
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            alpha = 0.35f,
                        ),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                    ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = "Cancel"
                        )
                    }
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            onTimeSelected(TimeSelected(hour, minute))
                            onDismiss()
                        }
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
@Suppress("AssignedValueIsNeverRead")
private fun AppTimePickerDialogPreview() {
    var selected by remember { mutableStateOf(TimeSelected(10, 20)) }

    AppTimePickerDialog(
        time = selected,
        onTimeSelected = { selected = it },
        onDismiss = {},
    )
}
