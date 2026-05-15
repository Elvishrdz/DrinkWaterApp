package com.eahm.drinkwaterapp.feature.dev.analytics

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
import com.eahm.drinkwaterapp.BuildConfig
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalyticsScreen(
    state: AnalyticsState,
    onAction: (AnalyticsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
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
                is AnalyticsState.Loading -> CircularProgressIndicator()
                is AnalyticsState.Data -> Content(state, onAction)
            }
        }
    }
}


@Composable
private fun Content(
    state: AnalyticsState.Data,
    onAction: (AnalyticsAction) -> Unit,
) {
    Column {
        Text("Analytics playground")
        Spacer(Modifier.height((12.dp)))

        Text("Crashlytics")
        @Suppress("KotlinConstantConditions")
        val isStaging = BuildConfig.IS_STAGING
        val exceptionMessage =
            "This is a test crash from version: ${state.versionName} and build: ${state.buildVariant}"
        DwPrimaryButton(
            text = "Send crash report now!",
            onClick = { throw Exception(exceptionMessage) },
            enabled = !isStaging,
        )
        if (isStaging) {
            Text("We dont report crashes in development. Use a QA build instead.")
        } else {
            Text("We will send: $exceptionMessage")
        }
    }
}

@Preview
@Composable
private fun ContentPreview() {
    Content(
        state = AnalyticsState.Data(
            versionName = "1.0.0",
            buildVariant = "qa",
        ),
        onAction = {}
    )
}
