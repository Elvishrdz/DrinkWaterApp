package com.eahm.drinkwaterapp.feature.dev.tracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackingDevScreen(
    state: TrackingDevState,
    onAction: (TrackingDevAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracking") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) { Text("← Back") }
                },
            )
        },
    ) { padding ->
        when (state) {
            TrackingDevState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is TrackingDevState.Data -> TrackingContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun TrackingContent(
    state: TrackingDevState.Data,
    onAction: (TrackingDevAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        // ── Mixpanel opt-in / opt-out ─────────────────────────────────
        Text("Mixpanel", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Simulate a user accepting or declining the tracking consent prompt.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tracking enabled",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = when {
                            state.isUpdatingTracking -> "Updating…"
                            state.isTrackingEnabled -> "Active — events are being sent"
                            else -> "Inactive — events are dropped"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isTrackingEnabled && !state.isUpdatingTracking)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Show a spinner while the opt-in/opt-out call is in flight
                if (state.isUpdatingTracking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Switch(
                        checked = state.isTrackingEnabled,
                        onCheckedChange = { onAction(TrackingDevAction.SetTrackingEnabled(it)) },
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Distinct ID",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = state.distinctId ?: "not available",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.distinctId != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // ── Test event ────────────────────────────────────────────────
        Text("Test event", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Send a test_event to Mixpanel with a custom parameter value to verify " +
                    "the pipeline end-to-end. Disabled in production builds.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.testEventParam,
            onValueChange = { onAction(TrackingDevAction.SetTestEventParam(it)) },
            label = { Text("Event parameter") },
            placeholder = { Text("e.g. button_click") },
            singleLine = true,
            enabled = state.canSendTestEvent,
        )

        DwPrimaryButton(
            text = "Send Test Event",
            enabled = state.canSendTestEvent
                    && state.isTrackingEnabled
                    && !state.isUpdatingTracking,
            onClick = { onAction(TrackingDevAction.SendTestEvent) },
        )

        // Contextual hint / feedback
        val hint = when {
            !state.canSendTestEvent ->
                "Test events are disabled in production builds."

            !state.isTrackingEnabled ->
                "Enable tracking first so events are not dropped."

            state.testEventParam.isBlank() ->
                "Enter a parameter value to include in the event."

            else ->
                "Will send: test_event { parameter: \"${state.testEventParam}\" }"
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Success feedback — pulses for 2 s after the event is dispatched
        AnimatedVisibility(
            visible = state.testEventSentFeedback,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                text = "✓ test_event dispatched to Mixpanel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // ── Force flush ───────────────────────────────────────────────
        Text("Force flush", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Push all queued events to the Mixpanel server immediately, " +
                    "without waiting for the SDK's next automatic flush.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        DwPrimaryButton(
            text = "Flush Events Now",
            onClick = { onAction(TrackingDevAction.FlushEvents) },
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Build: ${state.buildVariant}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingContentPreview() {
    DrinkWaterAppTheme {
        TrackingContent(
            state = TrackingDevState.Data(
                isTrackingEnabled = true,
                isUpdatingTracking = false,
                testEventParam = "button_click",
                canSendTestEvent = true,
                testEventSentFeedback = false,
                buildVariant = "qa",
                distinctId = "abc-123-preview",
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Updating")
@Composable
private fun TrackingContentUpdatingPreview() {
    DrinkWaterAppTheme {
        TrackingContent(
            state = TrackingDevState.Data(
                isTrackingEnabled = false,
                isUpdatingTracking = true,
                testEventParam = "",
                canSendTestEvent = true,
                testEventSentFeedback = false,
                buildVariant = "debug",
                distinctId = null,
            ),
            onAction = {},
        )
    }
}
