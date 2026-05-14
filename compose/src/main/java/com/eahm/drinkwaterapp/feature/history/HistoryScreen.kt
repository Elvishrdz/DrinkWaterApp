package com.eahm.drinkwaterapp.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.ui.historylist.DateTimePickerButton
import com.eahm.drinkwaterapp.ui.historylist.HistoryTimeline
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime

@Composable
internal fun HistoryScreen(
    state: HistoryState.Data,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onGoToToday: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Content(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            date = state.date,
            historyRecords = state.historyRecords,
            goalMl = state.goalMl,
            consumedMl = state.consumedMl,
            isToday = state.isToday,
            onDelete = onDelete,
            onDateChanged = onDateChanged,
            onGoToToday = onGoToToday,
        )
    }
}

@Composable
private fun Content(
    date: LocalDateTime,
    historyRecords: ImmutableList<HourGroup>,
    goalMl: Float,
    consumedMl: Float,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
    onGoToToday: () -> Unit,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.history_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionLabel(stringResource(R.string.history_section_select_date))
                    if (!isToday) {
                        TextButton(
                            onClick = onGoToToday,
                        ) {
                            Text(
                                text = stringResource(R.string.label_today),
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                DateTimePickerButton(
                    selectedDateTime = date,
                    onDateTimeSelected = onDateChanged,
                    label = stringResource(R.string.history_select_date_label),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.history_section_summary))
                DaySummaryRow(consumedMl = consumedMl, goalMl = goalMl)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel(
            text = stringResource(R.string.history_section_logs),
            modifier = Modifier.padding(horizontal = 22.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (historyRecords.isNotEmpty()) {
            HistoryTimeline(
                historyRecords = historyRecords,
                onDelete = { onDelete(it.toEntity()) },
            )
        } else {
            val text = if (isToday) {
                stringResource(R.string.drink_log_empty_state)
            } else {
                stringResource(R.string.history_empty_title)
            }

            Text(
                text = text,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun DaySummaryRow(
    consumedMl: Float,
    goalMl: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatChip(
            label = stringResource(R.string.history_stat_consumed),
            value = stringResource(R.string.drink_log_entry_amount, consumedMl.toInt().toString()),
            modifier = Modifier.weight(1f),
        )
        StatChip(
            label = stringResource(R.string.history_stat_goal),
            value = stringResource(R.string.drink_log_entry_amount, goalMl.toInt().toString()),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = DwInk300,
            letterSpacing = TextUnit(0.08f, TextUnitType.Em),
        )
        Text(
            text = value,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = DwInk500,
        letterSpacing = TextUnit(0.10f, TextUnitType.Em),
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun HistoryScreenPreview() {
    DrinkWaterAppTheme {
        Content(
            date = Mock.localDateTimeMock,
            historyRecords = Mock.hourGroupMock.toImmutableList(),
            goalMl = 2000f,
            consumedMl = 750f,
            isToday = false,
            onDelete = {},
            onDateChanged = {},
            onGoToToday = {},
        )
    }
}
