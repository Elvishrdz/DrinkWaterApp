package com.eahm.drinkwaterapp.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import com.eahm.drinkwaterapp.ui.historylist.DateTimePickerButton
import com.eahm.drinkwaterapp.ui.historylist.HistoryTimeline
import com.eahm.drinkwaterapp.ui.historylist.HourGroup
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoot() {
    val viewModel: HistoryViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val data = state) {
        HistoryState.Loading -> {
            /* Idle */
        }

        is HistoryState.Data -> {
            HistoryScreen(
                state = data,
                onDelete = viewModel::onDelete,
                onDateChanged = viewModel::onDateChanged,
            )
        }
    }
}

@Composable
internal fun HistoryScreen(
    state: HistoryState.Data,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DwInk50,
    ) { innerPadding ->
        Content(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            date = state.date,
            historyRecords = state.historyRecords,
            onDelete = onDelete,
            onDateChanged = onDateChanged,
        )
    }
}

@Composable
private fun Content(
    date: LocalDateTime,
    historyRecords: List<HourGroup>,
    modifier: Modifier = Modifier,
    onDelete: (HistoryEntity) -> Unit,
    onDateChanged: (LocalDateTime) -> Unit,
) {
    Column(modifier = modifier) {

        // ── Header (title + date selector) ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "History",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = DwInk900,
            )

            // Date section — label + styled date picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("SELECT DATE")
                DateTimePickerButton(
                    selectedDateTime = date,
                    onDateTimeSelected = onDateChanged,
                    label = "Select a date",
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Logs section label ───────────────────────────────────────────────
        SectionLabel(
            text = "LOGS",
            modifier = Modifier.padding(horizontal = 22.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── Timeline ─────────────────────────────────────────────────────────
        HistoryTimeline(
            historyRecords = historyRecords,
            onDelete = { onDelete(it.toEntity()) },
            modifier = Modifier.weight(1f),
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
            historyRecords = Mock.hourGroupMock,
            onDelete = {},
            onDateChanged = {},
        )
    }
}
