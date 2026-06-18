package com.eahm.drinkwaterapp.core.billing

import android.app.Application
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises the parts of [RevenueCatPurchaseManager] that do not touch the RevenueCat SDK: the
 * locally-managed [PurchaseManager.isAdFree] / [PurchaseManager.purchaseState] flows.
 *
 * These guard the contract the UI depends on — the Remove Ads button is shown off `isAdFree`, and
 * the purchase sheet reacts to `purchaseState`. Methods that call into `Purchases.sharedInstance`
 * (initialize/fetchOfferings/purchase/restore) require an instrumented/integration harness and are
 * intentionally out of scope here.
 */
class RevenueCatPurchaseManagerStateTest {

    private val manager = RevenueCatPurchaseManager(mockk<Application>(relaxed = true))

    @Test
    fun `default state is not ad-free and idle`() {
        assertFalse(manager.isAdFree.value)
        assertEquals(PurchaseState.Idle, manager.purchaseState.value)
    }

    @Test
    fun `devSetAdFree flips isAdFree and resets purchaseState to Idle`() {
        manager.devSetAdFree(true)
        assertTrue(manager.isAdFree.value)
        assertEquals(PurchaseState.Idle, manager.purchaseState.value)

        manager.devSetAdFree(false)
        assertFalse(manager.isAdFree.value)
    }

    @Test
    fun `cancel with no purchase in progress resets purchaseState to Idle without touching the SDK`() {
        // No purchase was started, so cancel() must not call into Purchases.sharedInstance.
        manager.cancel()
        assertEquals(PurchaseState.Idle, manager.purchaseState.value)
    }
}
