package com.eahm.drinkwaterapp.core.billing

import android.app.Application
import com.eahm.drinkwaterapp.system.initializer.AppInitializer
import io.mockk.mockk
import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Guards the Remove Ads wiring.
 *
 * Regression test for the prod bug where two separate `singleOf(::RevenueCatPurchaseManager)`
 * definitions produced TWO instances: BaseApplication called `initialize()` (Purchases.configure +
 * refreshCustomerInfo) on the [AppInitializer]-bound one, while the entire UI observed the
 * [PurchaseManager]-bound one. The observed instance was never initialized or refreshed on launch,
 * so a user who already purchased ad-free saw ads (and the Remove Ads button) come back on every
 * relaunch.
 *
 * The contract these tests lock in: exactly one [RevenueCatPurchaseManager] instance backs both the
 * [AppInitializer] (so it gets initialized at startup) and the [PurchaseManager] (the flows the UI
 * reads). If anyone splits the binding again, these tests fail before the regression ships.
 */
class BillingModuleTest : KoinTest {

    private val purchaseManager: PurchaseManager by inject()

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun startBilling() {
        startKoin {
            modules(
                module { single<Application> { mockk(relaxed = true) } },
                billingModule,
            )
        }
    }

    @Test
    fun `RevenueCatPurchaseManager is registered as the only AppInitializer of its type`() {
        startBilling()

        val revenueCatInitializers = getKoin()
            .getAll<AppInitializer>()
            .filterIsInstance<RevenueCatPurchaseManager>()

        assertEquals(
            1,
            revenueCatInitializers.size,
            "Expected exactly one RevenueCatPurchaseManager bound as AppInitializer",
        )
    }

    @Test
    fun `the initialized AppInitializer instance is the same one the UI observes as PurchaseManager`() {
        startBilling()

        // The instance BaseApplication runs initialize() on at startup.
        val initialized = getKoin()
            .getAll<AppInitializer>()
            .filterIsInstance<RevenueCatPurchaseManager>()
            .single()

        // The instance every screen injects and observes isAdFree/purchaseState from.
        val observed = purchaseManager

        assertTrue(
            observed is RevenueCatPurchaseManager,
            "PurchaseManager should be backed by RevenueCatPurchaseManager in production wiring",
        )
        assertSame(
            initialized,
            observed,
            "The initialized purchase manager must be the very same instance the UI observes; " +
                    "otherwise ad-free state from a real purchase never reaches the UI on launch.",
        )
    }
}
