package com.eahm.drinkwaterapp.feature.dev.alarms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmsScreen(
    state: AlarmsState,
    onAction: (AlarmsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current


    val lifecycleState by lifecycle.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

    when (lifecycleState) {
        Lifecycle.State.INITIALIZED -> {}
        Lifecycle.State.CREATED -> {}
        Lifecycle.State.STARTED -> {}
        Lifecycle.State.RESUMED -> {
            onAction(AlarmsAction.OnResumeCalled)
        }

        Lifecycle.State.DESTROYED -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms") },
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
                is AlarmsState.Loading -> CircularProgressIndicator()
                is AlarmsState.Data -> DataContent(state)
            }
        }
    }
}

@Composable
private fun DataContent(state: AlarmsState.Data) {
    Column {
        Text("Alarms playground")
        Text("Has notification permission: ${state.hasNotificationPermissions} ")
        Text("Is drink alarm active: ${state.isAlarmActive} ")
    }
}

@Preview
@Composable
private fun AlarmsScreenPreview() {
    AlarmsScreen(
        state = alarmStateMock,
        onAction = {},
        onBackClicked = {},
    )
}

private val alarmStateMock = AlarmsState.Data(
    hasNotificationPermissions = true,
    isAlarmActive = true,
)