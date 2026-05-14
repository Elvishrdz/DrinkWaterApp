package com.eahm.drinkwaterapp.core.billing

import android.app.Activity
import android.app.Application
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.revenuecat.purchases.Package as RcPackage

private const val ENTITLEMENT_AD_FREE = "ad_free"

internal class PurchaseManager(
    private val application: Application,
) : AppInitializer {

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    override fun initialize() {
        val apiKey = application.getString(R.string.revenuecat_api_key)
        Purchases.configure(
            PurchasesConfiguration.Builder(application, apiKey).build()
        )
        refreshCustomerInfo()
    }

    fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
            }

            override fun onError(error: PurchasesError) {
                // retain last known state — don't flip to false on network error
            }
        })
    }

    fun fetchOfferings(onResult: (RcPackage?) -> Unit) {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val pkg = offerings["remove_ads"]?.availablePackages?.firstOrNull()
                    ?: offerings.current?.availablePackages?.firstOrNull()
                onResult(pkg)
            }

            override fun onError(error: PurchasesError) {
                onResult(null)
            }
        })
    }

    fun purchase(activity: Activity, pkg: RcPackage) {
        _purchaseState.value = PurchaseState.Loading
        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(
                    storeTransaction: StoreTransaction,
                    customerInfo: CustomerInfo
                ) {
                    _isAdFree.value =
                        customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                    _purchaseState.value =
                        if (_isAdFree.value) PurchaseState.Success else PurchaseState.Idle
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _purchaseState.value = if (userCancelled) {
                        PurchaseState.Idle
                    } else {
                        PurchaseState.Error(error.message)
                    }
                }
            }
        )
    }

    fun restore() {
        _purchaseState.value = PurchaseState.Loading
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_AD_FREE]?.isActive == true
                _purchaseState.value = if (_isAdFree.value) {
                    PurchaseState.Success
                } else {
                    PurchaseState.NothingToRestore
                }
            }

            override fun onError(error: PurchasesError) {
                _purchaseState.value = PurchaseState.Error(error.message)
            }
        })
    }
}

sealed interface PurchaseState {
    data object Idle : PurchaseState
    data object Loading : PurchaseState
    data object Success : PurchaseState
    data object NothingToRestore : PurchaseState
    data class Error(val message: String) : PurchaseState
}
