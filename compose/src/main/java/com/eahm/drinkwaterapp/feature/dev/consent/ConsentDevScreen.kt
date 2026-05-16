package com.eahm.drinkwaterapp.feature.dev.consent

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ConsentDevScreen(
    privacyPolicyAcceptedAt: Long?,
    trackingConsentDecided: Boolean,
    isTracking: Boolean,
    onResetPrivacyPolicy: () -> Unit,
    onResetTrackingConsentDecided: () -> Unit,
    onResetAll: () -> Unit,
    onSimulateReturnAtTrackingStep: () -> Unit,
    onLaunchConsent: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val ppTimestamp = privacyPolicyAcceptedAt
        ?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) }
        ?: "null"

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
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("State", fontWeight = FontWeight.SemiBold)
            }

            item {
                Text(
                    text = "Privacy Policy accepted at: $ppTimestamp",
                    fontSize = 13.sp,
                )
            }

            item {
                Text(
                    text = "Tracking consent decided: $trackingConsentDecided",
                    fontSize = 13.sp,
                )
            }

            item {
                Text(
                    text = "Tracking active: $isTracking",
                    fontSize = 13.sp,
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Actions", fontWeight = FontWeight.SemiBold)
            }

            item {
                OutlinedButton(
                    onClick = onResetPrivacyPolicy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset privacy policy")
                }
            }

            item {
                OutlinedButton(
                    onClick = onResetTrackingConsentDecided,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset tracking consent decided")
                }
            }

            item {
                OutlinedButton(
                    onClick = onResetAll,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset all consent")
                }
            }

            item {
                OutlinedButton(
                    onClick = onSimulateReturnAtTrackingStep,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Simulate return at tracking step (PP accepted, tracking undecided)")
                }
            }

            item {
                Button(
                    onClick = onLaunchConsent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Launch consent screen")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
