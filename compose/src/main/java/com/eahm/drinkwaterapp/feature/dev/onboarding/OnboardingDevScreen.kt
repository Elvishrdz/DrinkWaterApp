package com.eahm.drinkwaterapp.feature.dev.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun OnboardingDevScreen(
    isCompleted: Boolean,
    onReset: () -> Unit,
    onStartOnboarding: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBackClicked, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Menu")
                }
            }

            item {
                Text(
                    text = "Onboarding completed: $isCompleted",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Actions", fontWeight = FontWeight.SemiBold)
            }

            item {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset onboarding (clear preference)")
                }
            }

            item {
                Button(
                    onClick = onStartOnboarding,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start onboarding flow")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
