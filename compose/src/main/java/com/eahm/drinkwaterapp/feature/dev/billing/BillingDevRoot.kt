package com.eahm.drinkwaterapp.feature.dev.billing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import org.koin.compose.koinInject

@Composable
internal fun BillingDevRoot(
    onBackClicked: () -> Unit,
    purchaseManager: PurchaseManager = koinInject(),
) {
    BackHandler(onBack = onBackClicked)

    val isAdFree by purchaseManager.isAdFree.collectAsStateWithLifecycle()
    val purchaseState by purchaseManager.purchaseState.collectAsStateWithLifecycle()

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
                SectionHeader("Current State")
                DataRow("isAdFree", "$isAdFree")
                DataRow("purchaseState", purchaseState::class.simpleName ?: "-")
            }

            item {
                HorizontalDivider()
                SectionHeader("Override (dev only)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { purchaseManager.devSetAdFree(true) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Ad Free ON")
                    }
                    OutlinedButton(
                        onClick = { purchaseManager.devSetAdFree(false) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Ad Free OFF")
                    }
                }
            }

            item {
                HorizontalDivider()
                SectionHeader("Simulate purchase flow")
                Text(
                    text = "These buttons simulate the full purchase/restore UI flow as a real user would trigger it from Settings → Remove Ads.",
                    color = DwInk500,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Button(
                    onClick = { purchaseManager.devSetAdFree(false) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset to no purchase")
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "After resetting, go to Settings → Remove Ads and tap the button to simulate a purchase.",
                    color = DwInk500,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = DwInk500, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
