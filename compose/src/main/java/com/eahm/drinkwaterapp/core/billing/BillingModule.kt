package com.eahm.drinkwaterapp.core.billing

import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module

internal val billingModule = module {
    //singleOf(::TestPurchaseManager) bind PurchaseManager::class

    // A SINGLE RevenueCatPurchaseManager instance must back both interfaces. The
    // AppInitializer-bound instance is the one BaseApplication calls initialize() on
    // (Purchases.configure + refreshCustomerInfo), while the whole UI observes the
    // PurchaseManager-bound instance's isAdFree/purchaseState flows. Two separate
    // `singleOf` definitions create two separate instances, so the initialized one and
    // the observed one differ — a restored/purchased user's ad-free state never reaches
    // the UI on launch and ads reappear. `binds` keeps it to one instance for both.
    singleOf(::RevenueCatPurchaseManager) binds arrayOf(
        AppInitializer::class,
        PurchaseManager::class,
    )
}
