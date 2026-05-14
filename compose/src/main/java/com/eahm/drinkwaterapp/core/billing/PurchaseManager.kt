package com.eahm.drinkwaterapp.core.billing

import android.app.Activity
import android.app.Application
import com.eahm.drinkwaterapp.BuildConfig
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
        if (BuildConfig.IS_DEV) return  // skip RevenueCat in dev — use devSetAdFree() to test

        Purchases.configure(
            PurchasesConfiguration.Builder(application, BuildConfig.REVENUECAT_API_KEY).build()
        )
        refreshCustomerInfo()
    }

    fun refreshCustomerInfo() {
        if (BuildConfig.IS_DEV) return
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
        if (BuildConfig.IS_DEV) {
            onResult(null)  // no price in dev — button shows "Remove Ads" without price
            return
        }
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
        if (BuildConfig.IS_DEV) {
            _purchaseState.value = PurchaseState.Loading
            _isAdFree.value = true
            _purchaseState.value = PurchaseState.Success
            return
        }
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
        if (BuildConfig.IS_DEV) {
            _purchaseState.value = PurchaseState.Loading
            _purchaseState.value = if (_isAdFree.value) {
                PurchaseState.Success
            } else {
                PurchaseState.NothingToRestore
            }
            return
        }
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

    /** Only meaningful in dev/debug builds. Directly sets the ad-free state for testing. */
    fun devSetAdFree(value: Boolean) {
        if (!BuildConfig.IS_DEV) return
        _isAdFree.value = value
        _purchaseState.value = PurchaseState.Idle
    }
}

sealed interface PurchaseState {
    data object Idle : PurchaseState
    data object Loading : PurchaseState
    data object Success : PurchaseState
    data object NothingToRestore : PurchaseState
    data class Error(val message: String) : PurchaseState
}
