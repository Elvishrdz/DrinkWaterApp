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
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.recordsMock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryButton(
    text: String,
    records: List<HistoryUI>,
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
            records = records,
            onDelete = onDelete,
            onDismiss = { showModal = false },
        )
    }
}

@Preview
@Composable
private fun HistoryButtonPreview() {
    HistoryButton(
        text = "Time",
        records = Mock.recordsMock.toUI(),
        onDelete = {},
    )
}
