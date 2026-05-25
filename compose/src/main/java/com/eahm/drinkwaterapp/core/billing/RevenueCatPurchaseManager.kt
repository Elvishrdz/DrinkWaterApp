package com.eahm.drinkwaterapp.core.billing

import android.app.Activity
import android.app.Application
import com.eahm.drinkwaterapp.BuildConfig
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
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

private const val ENTITLEMENT_ADS_FREE = "ads_free"
private const val OFFERING_REMOVE_ADS = "remove_ads"

internal class RevenueCatPurchaseManager(
    private val application: Application,
) : AppInitializer, PurchaseManager {

    private val _isAdFree = MutableStateFlow(false)
    override val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    override fun initialize() {
        val config = PurchasesConfiguration.Builder(application, BuildConfig.REVENUECAT_API_KEY)

        Purchases.configure(config.build())
        refreshCustomerInfo()
    }

    override fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_ADS_FREE]?.isActive == true
            }

            override fun onError(error: PurchasesError) {
                // retain last known state — don't flip to false on network error
            }
        })
    }

    override fun fetchOfferings(
        onSuccess: (Package?) -> Unit,
        onError: () -> Unit,
    ) {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                val availablePackage =
                    offerings[OFFERING_REMOVE_ADS]?.availablePackages?.firstOrNull()
                        ?: offerings.current?.availablePackages?.firstOrNull()
                onSuccess(availablePackage)
            }

            override fun onError(error: PurchasesError) {
                onError()
            }
        })
    }

    private var purchaseInProgress = false
    override fun purchase(activity: Activity, pkg: Package) {
        _purchaseState.value = PurchaseState.Loading
        purchaseInProgress = true
        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(
                    storeTransaction: StoreTransaction,
                    customerInfo: CustomerInfo
                ) {
                    purchaseInProgress = false
                    _isAdFree.value =
                        customerInfo.entitlements[ENTITLEMENT_ADS_FREE]?.isActive == true
                    _purchaseState.value =
                        if (_isAdFree.value) PurchaseState.Success else PurchaseState.Idle
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    purchaseInProgress = false
                    _purchaseState.value = if (userCancelled) {
                        PurchaseState.Idle
                    } else {
                        PurchaseState.Error(error.message)
                    }
                }
            }
        )
    }

    override fun cancel() {
        runCatching {
            // close and initialize. This in the case the sheet is closed and a purchase was in progress
            // Usually this happens only in dev builds, when rotating the screen and losing the purchase
            // state.
            if (purchaseInProgress) {
                // TODO: track if this happens
                Purchases.Companion.sharedInstance.close()
                if (!Purchases.Companion.isConfigured) {
                    initialize()
                }
            }
        }

        _purchaseState.value = PurchaseState.Idle
    }

    override fun restore() {
        _purchaseState.value = PurchaseState.Loading
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isAdFree.value = customerInfo.entitlements[ENTITLEMENT_ADS_FREE]?.isActive == true
                _purchaseState.value = if (_isAdFree.value) {
                    PurchaseState.Restored
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
    override fun devSetAdFree(value: Boolean) {
        _isAdFree.value = value
        _purchaseState.value = PurchaseState.Idle
    }
}
