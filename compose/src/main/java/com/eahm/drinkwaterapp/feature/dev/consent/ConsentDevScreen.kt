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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ConsentDevScreen(
    privacyPolicyAcceptedAt: Long?,
    trackingConsentDecided: Boolean,
    isTracking: Boolean,
    privacyPolicyVersionAccepted: Long?,
    currentPolicyDate: Long,
    isPolicyUpToDate: Boolean,
    onResetPrivacyPolicy: () -> Unit,
    onResetTrackingConsentDecided: () -> Unit,
    onResetAll: () -> Unit,
    setPrivacyPolicyAcceptedNow: () -> Unit,
    onAcceptPolicyVersionNow: () -> Unit,
    onSimulatePolicyUpdate: () -> Unit,
    onLaunchConsent: () -> Unit,
    onBackClicked: () -> Unit,
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val ppTimestamp = privacyPolicyAcceptedAt
        ?.let { dateFormat.format(Date(it)) }
        ?: "null"
    val versionAcceptedFormatted = privacyPolicyVersionAccepted
        ?.let { dateFormat.format(Date(it)) }
        ?: "null"
    val currentPolicyDateFormatted = dateFormat.format(Date(currentPolicyDate))

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
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }

            item {
                Text(
                    text = "Tracking consent decided: $trackingConsentDecided",
                    fontWeight = FontWeight.Medium,
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
                Text(
                    text = "Privacy Policy version accepted: $versionAcceptedFormatted",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }

            item {
                Text(
                    text = "Current policy date: $currentPolicyDateFormatted",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }

            item {
                Text(
                    text = "Policy up to date: $isPolicyUpToDate",
                    fontSize = 13.sp,
                )
            }

            item { Spacer(Modifier.height(7.dp)) }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Actions", fontWeight = FontWeight.SemiBold)
            }

            item {
                DwTertiaryButton(
                    text = "Accept privacy policy now",
                    onClick = setPrivacyPolicyAcceptedNow,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                DwTertiaryButton(
                    text = "Accept policy version now",
                    onClick = onAcceptPolicyVersionNow,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                OutlinedButton(
                    onClick = onSimulatePolicyUpdate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Simulate policy update (clear version)")
                }
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

            item { Spacer(Modifier.height(7.dp)) }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Launch", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "REMEMBER: state cache will replay when launching from dev menu. Wait the given SharingStarted.WhileSubscribed({Time-HERE}}) to start with a fresh state.",
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
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
