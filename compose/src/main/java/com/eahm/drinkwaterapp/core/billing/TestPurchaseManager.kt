package com.eahm.drinkwaterapp.core.billing

import android.app.Activity
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestPurchaseManager : PurchaseManager {

    override val isAdFree: StateFlow<Boolean> = MutableStateFlow(true)
    override val purchaseState: StateFlow<PurchaseState> = MutableStateFlow(PurchaseState.Idle)

    override fun refreshCustomerInfo() {}
    override fun fetchOfferings(
        onSuccess: (Package?) -> Unit,
        onError: () -> Unit
    ) {
    }

    override fun purchase(activity: Activity, pkg: Package) {}
    override fun cancel() {}
    override fun restore() {}
    override fun devSetAdFree(value: Boolean) {}
}
