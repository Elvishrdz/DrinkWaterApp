package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import kotlinx.datetime.LocalDateTime
import java.util.Calendar
import java.util.Locale
import kotlin.time.ExperimentalTime

data class HistoryUI(
    val id: String,
    val amount: String,
    val status: Int,
    val timestamp: LocalDateTime,
)

data class DayGroup(
    val hour: Int,
    val day: Int,
    val month: Int,
    val year: Int,
)

data class HourGroup(
    val hour: Int,
    val isCurrentHour: Boolean,
    val items: List<HistoryUI>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    date: LocalDateTime,
    historyRecords: List<HourGroup>,
    onDelete: (HistoryUI) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
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
                    text = "History",
                )

                DateTimePickerButton(
                    selectedDateTime = date,
                    onDateTimeSelected = onDateChanged,
                    label = "Select a date",
                )

                HistoryTimeline(
                    historyRecords = historyRecords,
                    onDelete = onDelete,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HistoryDialogPreview() {
    HistoryDialog(
        date = Mock.localDateTimeMock,
        historyRecords = Mock.hourGroupMock,
        onDismiss = {},
        onDelete = {},
        onDateChanged = {},
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun HistoryTimeline(
    historyRecords: List<HourGroup>,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historyRecords) { hourGroup ->
            TimelineHourGroup(
                hourGroup = hourGroup,
                isCurrentHour = hourGroup.isCurrentHour,
                onDelete = onDelete,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun TimelineHourGroup(
    hourGroup: HourGroup,
    isCurrentHour: Boolean,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%02d:00", hourGroup.hour,
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentHour) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (isCurrentHour) {
                    Text(
                        text = "Ahora",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            hourGroup.items.forEach { item ->
                TimelineItemCard(
                    item = item,
                    onDelete = onDelete,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TimelineItemCard(
    item: HistoryUI,
    onDelete: (HistoryUI) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(getIconResId(item.timestamp)),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            val unit = stringResource(R.string.text_vol_unit_mililiters)

            Text(
                text = "${item.amount}$unit",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = formatTime(item.timestamp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(
            onClick = { onDelete(item) },
        ) {
            Text("Delete")
        }
    }
}

private fun formatTime(timestamp: LocalDateTime): String {
    val hour = timestamp.hour
    val minute = timestamp.minute
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

@OptIn(ExperimentalTime::class)
fun getIconResId(timestamp: LocalDateTime): Int {
    val hour = timestamp.hour  // 0-23
    val secondsSinceMidnight = hour * 3600

    val startMadrugada = 1 * 3600      // 1:00 AM
    val startManana = 5 * 3600         // 5:00 AM
    val startDay = 10 * 3600           // 10:00 AM
    val startTarde = 15 * 3600         // 3:00 PM
    val startNight = 19 * 3600         // 7:00 PM

    return when (secondsSinceMidnight) {
        in startMadrugada..<startManana -> R.drawable.icon_r_madrugada  // 1-4
        in startManana..<startDay -> R.drawable.icon_r_manana            // 5-9
        in startDay..<startTarde -> R.drawable.icon_r_dia                // 10-14
        in startTarde..<startNight -> R.drawable.icon_r_tarde            // 15-18
        else -> R.drawable.icon_r_noche                                  // 19-23, 0-0
    }
}