package com.eahm.drinkwaterapp.feature.dev.alarmintervals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmIntervalsScreen(
    state: AlarmIntervalsState,
    onAction: (AlarmIntervalsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intervals") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) {
                        Text("← Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is AlarmIntervalsState.Loading -> CircularProgressIndicator()
                is AlarmIntervalsState.Data -> Text("Intervals playground")
            }
        }
    }
}

@Preview
@Composable
private fun AlarmIntervalsScreenPreview() {
    AlarmIntervalsScreen(
        state = alarmIntervalsStateMock,
        onAction = {},
        onBackClicked = {},
    )
}

private val alarmIntervalsStateMock = AlarmIntervalsState.Data()
