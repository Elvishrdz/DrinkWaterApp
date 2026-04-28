package com.eahm.drinkwaterapp.feature.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.bindings.toEntity
import com.eahm.drinkwaterapp.core.database.entity.HistoryEntity
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
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
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    Column(
        modifier = modifier,
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
            onDelete = { onDelete(it.toEntity()) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
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
