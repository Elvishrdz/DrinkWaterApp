package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.billing.PurchaseState
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.settings.OfferingsState
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RemoveAdsBottomSheet(
    offeringsState: OfferingsState,
    purchaseState: PurchaseState,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    AppBottomSheet(
        onDismissRequest = { dismissWithAnimation() },
        sheetState = sheetState,
    ) {
        val isLoading = purchaseState is PurchaseState.Loading

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_remove_ads_title),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_remove_ads_body),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DwInk500,
            )

            Spacer(Modifier.height(24.dp))

            when (offeringsState) {
                is OfferingsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DwPrimaryButton(
                            text = "",
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CircularProgressIndicator(
                            color = DwInk500,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                is OfferingsState.Error -> {
                    DwPrimaryButton(
                        text = stringResource(R.string.action_retry),
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is OfferingsState.Success -> {
                    val priceString = offeringsState.pkg?.product?.price?.formatted
                    DwPrimaryButton(
                        text = if (priceString != null) {
                            stringResource(R.string.settings_remove_ads_cta_with_price, priceString)
                        } else {
                            stringResource(R.string.settings_remove_ads_cta)
                        },
                        onClick = onPurchase,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            DwSecondaryButton(
                text = stringResource(R.string.settings_remove_ads_restore_cta),
                onClick = onRestore,
                enabled = !isLoading && offeringsState is OfferingsState.Success,
                modifier = Modifier.fillMaxWidth(),
            )

            when (val s = purchaseState) {
                is PurchaseState.Error -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.settings_remove_ads_error, s.message),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                PurchaseState.NothingToRestore -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.settings_remove_ads_nothing_to_restore),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DwInk500,
                    )
                }

                PurchaseState.Loading -> {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = DwMagenta,
                        modifier = Modifier.size(24.dp),
                    )
                }

                else -> {}
            }
        }
    }
}
