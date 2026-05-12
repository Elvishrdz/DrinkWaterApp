package com.eahm.drinkwaterapp.feature.dev.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.ui.timeselector.AppTimeSelectorButton
import com.eahm.drinkwaterapp.ui.timeselector.TimeSelected

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreen(
    state: NotificationsState,
    onAction: (NotificationsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
                is NotificationsState.Loading -> CircularProgressIndicator()
                is NotificationsState.Data -> Content(state, onAction)
            }
        }
    }
}

@Composable
private fun Content(
    state: NotificationsState.Data,
    onAction: (NotificationsAction) -> Unit,
) {
    Column {
        Text("Notifications playground")

        Text("Has notification permission: ${state.hasNotificationPermissions}")
        Text("Configuration")
        Text("Hour extra parameter: ${state.extraTime.hour}")
        Text("Minute extra parameter: ${state.extraTime.minute}")

        if (state.hasNotificationPermissions) {
            AppTimeSelectorButton(
                text = "Change time extra for notification",
                onTimeSelected = {
                    onAction(NotificationsAction.OnExtraTimeChanged(it))
                },
            )

            Spacer(Modifier.height(12.dp))

            DwPrimaryButton(
                text = "Show notification",
                onClick = {
                    onAction(NotificationsAction.ShowNotification)
                }
            )

            Spacer(Modifier.height(7.dp))

            DwPrimaryButton(
                text = "Show notification (using broadcast)",
                onClick = {
                    onAction(NotificationsAction.ShowNotificationUsingBroadcast)
                }
            )
        }
    }
}

@Preview
@Composable
private fun ContentPreview() {
    Content(
        state = NotificationsState.Data(
            hasNotificationPermissions = true,
            extraTime = TimeSelected(hour = 7, minute = 30),
        ),
        onAction = {},
    )
}
