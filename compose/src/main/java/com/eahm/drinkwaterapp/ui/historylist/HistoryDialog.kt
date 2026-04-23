package com.eahm.drinkwaterapp.ui.historylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eahm.drinkwaterapp.core.bindings.toUI
import com.eahm.drinkwaterapp.core.domain.mock.Mock
import com.eahm.drinkwaterapp.core.domain.mock.recordsMock
import kotlinx.datetime.LocalDateTime

data class HistoryUI(
    val id: String,
    val amount: Float,
    val status: Int,
    val timestamp: LocalDateTime,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    records: List<HistoryUI>,
    onDelete: (HistoryUI) -> Unit,
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
                    text = "Set time",
                )

                LazyColumn {
                    items(records) { item ->
                        Row(modifier = Modifier.animateItem()) {
                            Column {
                                Text(
                                    text = item.id.takeLast(7),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = item.amount.toString(),
                                )
                                Text(
                                    text = item.timestamp.toString(),
                                )
                            }
                            Button(
                                onClick = { onDelete(item) },
                            ) {
                                Text(
                                    text = "Delete",
                                )
                            }
                        }
                    }
                }

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
        records = Mock.recordsMock.toUI(),
        onDismiss = {},
        onDelete = {},
    )
}
