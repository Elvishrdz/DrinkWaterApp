package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.hourGroupMock
import com.eahm.drinkwaterapp.core.domain.mock.localDateTimeMock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryButton(
    text: String,
    date: LocalDateTime,
    historyRecords: ImmutableList<HourGroup>,
    onDateChanged: (LocalDateTime) -> Unit,
    onDelete: (HistoryUI) -> Unit,
) {
    var showModal by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showModal = !showModal },
    ) {
        Text(text)
    }

    if (showModal) {
        HistoryDialog(
            date = date,
            historyRecords = historyRecords,
            onDelete = onDelete,
            onDateChanged = onDateChanged,
            onDismiss = { showModal = false },
        )
    }
}

@Preview
@Composable
private fun HistoryButtonPreview() {
    HistoryButton(
        text = "Time",
        date = Mock.localDateTimeMock,
        historyRecords = Mock.hourGroupMock.toImmutableList(),
        onDateChanged = {},
        onDelete = {},
    )
}
