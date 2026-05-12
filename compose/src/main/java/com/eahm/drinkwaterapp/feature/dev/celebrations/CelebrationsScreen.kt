package com.eahm.drinkwaterapp.feature.dev.celebrations

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CelebrationsScreen(
    state: CelebrationsState,
    onAction: (CelebrationsAction) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Celebrations") },
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
                is CelebrationsState.Loading -> CircularProgressIndicator()
                is CelebrationsState.Data -> Content(onAction)
            }
        }
    }
}

@Composable
private fun Content(onAction: (CelebrationsAction) -> Unit) {
    Column {
        Text("Celebrations playground")

        DwPrimaryButton(
            text = "Day",
            onClick = { onAction(CelebrationsAction.ShowDayCelebration) },
        )
    }
}

@Preview
@Composable
private fun ContentPreview() {
    Content(
        onAction = {},
    )
}
