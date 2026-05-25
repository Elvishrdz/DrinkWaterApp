package com.eahm.drinkwaterapp.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow
import com.revenuecat.purchases.Package as AvailablePackage

interface PurchaseManager {
    val isAdFree: StateFlow<Boolean>
    val purchaseState: StateFlow<PurchaseState>
    fun refreshCustomerInfo()
    fun fetchOfferings(
        onSuccess: (AvailablePackage?) -> Unit,
        onError: () -> Unit,
    )

    fun purchase(activity: Activity, pkg: AvailablePackage)
    fun cancel()
    fun restore()
    fun devSetAdFree(value: Boolean)
}
