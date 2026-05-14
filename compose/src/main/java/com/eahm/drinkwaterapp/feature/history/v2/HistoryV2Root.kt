package com.eahm.drinkwaterapp.feature.history.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.history.ui.DayStripRow
import com.eahm.drinkwaterapp.feature.history.ui.EmptyState
import com.eahm.drinkwaterapp.feature.history.ui.HistoryHeader
import com.eahm.drinkwaterapp.feature.history.ui.HourGroupCard
import com.eahm.drinkwaterapp.feature.history.ui.SummaryCard
import com.eahm.drinkwaterapp.ui.historylist.HistoryUI
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HistoryV2Root(
    viewModel: HistoryV2ViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HistoryV2Content(
        state = state,
        onSelectDay = viewModel::onSelectDay,
        onPrevDay = viewModel::onPrevDay,
        onNextDay = viewModel::onNextDay,
        onDelete = viewModel::onDelete,
        onUpdateRecord = viewModel::onUpdateRecord,
    )
}

@Composable
internal fun HistoryV2Content(
    state: HistoryV2State,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onUpdateRecord: (HistoryEntity) -> Unit,
) {
    when (state) {
        HistoryV2State.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = DwWater)
        }

        is HistoryV2State.Data -> HistoryV2Screen(
            state = state,
            onSelectDay = onSelectDay,
            onPrevDay = onPrevDay,
            onNextDay = onNextDay,
            onDelete = onDelete,
            onUpdateRecord = onUpdateRecord,
        )
    }
}

@Composable
internal fun HistoryV2Screen(
    state: HistoryV2State.Data,
    onSelectDay: (String) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDelete: (HistoryEntity) -> Unit,
    onUpdateRecord: (HistoryEntity) -> Unit,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var editingRecord by remember { mutableStateOf<HistoryUI?>(null) }
    var editAmountText by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DwInk50),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            HistoryHeader()

            DayStripRow(
                days = state.days,
                selectedKey = state.selectedDayKey,
                canGoPrev = state.canGoPrev,
                canGoNext = state.canGoNext,
                onSelectDay = onSelectDay,
                onPrevDay = onPrevDay,
                onNextDay = onNextDay,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SummaryCard(
                        dayLabel = state.selectedDayLabel,
                        totalMl = state.totalMl,
                        drinkCount = state.drinkCount,
                        percent = state.percent,
                    )
                }

                if (state.records.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(state.records, key = { it.hour }) { group ->
                        HourGroupCard(
                            group = group,
                            expandedId = expandedId,
                            onToggleExpand = { id ->
                                expandedId = if (expandedId == id) null else id
                            },
                            onEdit = { record ->
                                editingRecord = record
                                editAmountText = record.amount
                            },
                            onDelete = { record ->
                                onDelete(record.toEntity())
                                expandedId = null
                            },
                        )
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { editingRecord = null },
            containerColor = DwPaper,
            title = {
                Text(
                    text = stringResource(R.string.history_edit_dialog_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = DwInk900,
                )
            },
            text = {
                OutlinedTextField(
                    value = editAmountText,
                    onValueChange = { editAmountText = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.history_edit_amount_label)) },
                    suffix = { Text(stringResource(R.string.volume_unit_mililiters)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editAmountText.toFloatOrNull()
                        if (amount != null && amount > 0f) {
                            onUpdateRecord(record.toEntity().copy(amount = amount))
                            editingRecord = null
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_save),
                        color = DwMagenta,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecord = null }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = DwInk500,
                        fontFamily = PlusJakartaSans,
                    )
                }
            },
        )
    }
}


private val previewDays = listOf(
    DayItem("2026-05-08", "Thu", "8", "Thu, May 8", false, true, true),
    DayItem("2026-05-09", "Fri", "9", "Fri, May 9", false, true, false),
    DayItem("2026-05-10", "Sat", "10", "Today, May 10", true, true, false),
)

private val previewRecords = listOf(
    HourGroup(
        hour = 8,
        isCurrentHour = false,
        items = listOf(
            HistoryUI("1", "250", 1, LocalDateTime(2026, 5, 10, 8, 30)),
        ),
    ),
    HourGroup(
        hour = 12,
        isCurrentHour = false,
        items = listOf(
            HistoryUI("2", "300", 1, LocalDateTime(2026, 5, 10, 12, 15)),
            HistoryUI("3", "250", 1, LocalDateTime(2026, 5, 10, 12, 45)),
        ),
    ),
)

private val previewState = HistoryV2State.Data(
    days = previewDays,
    selectedDayKey = "2026-05-10",
    selectedDayLabel = "Today, May 10",
    totalMl = 1650,
    drinkCount = 6,
    goalMl = 2200,
    percent = 75,
    records = previewRecords,
    canGoPrev = true,
    canGoNext = false,
)

@Preview(showBackground = true)
@Composable
private fun HistoryV2ScreenPreview() {
    DrinkWaterAppTheme {
        HistoryV2Screen(
            state = previewState,
            onSelectDay = {},
            onPrevDay = {},
            onNextDay = {},
            onDelete = {},
            onUpdateRecord = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryV2ScreenEmptyPreview() {
    DrinkWaterAppTheme {
        HistoryV2Screen(
            state = previewState.copy(
                totalMl = 0,
                drinkCount = 0,
                percent = 0,
                records = emptyList(),
            ),
            onSelectDay = {},
            onPrevDay = {},
            onNextDay = {},
            onDelete = {},
            onUpdateRecord = {},
        )
    }
}
