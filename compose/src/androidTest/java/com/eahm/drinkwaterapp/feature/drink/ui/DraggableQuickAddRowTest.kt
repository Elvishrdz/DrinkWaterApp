package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [DraggableQuickAddRow].
 *
 * These tests verify that drag-to-reorder gestures result in the correct list
 * being delivered to onReorder. The composable uses test-tagged chips
 * ("chip_<amountInt>", e.g. "chip_100") so nodes can be located by preset.
 *
 * Gesture simulation:
 *   1. down — press the chip
 *   2. advanceEventTime past the long-press timeout — triggers onDragStart
 *   3. moveBy a distance large enough to cross the swap threshold — triggers onDrag
 *   4. up — triggers onDragEnd which calls onReorder
 */
@RunWith(AndroidJUnit4::class)
class DraggableQuickAddRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val presetA = DrinkSuggestion(order = 1, amountMl = 100f)
    private val presetB = DrinkSuggestion(order = 2, amountMl = 200f)
    private val presetC = DrinkSuggestion(order = 3, amountMl = 300f)

    @Test
    fun movingOneItemRightCallsOnReorderWithUpdatedList() {
        var capturedOrder: List<DrinkSuggestion>? = null

        composeTestRule.setContent {
            DrinkWaterAppTheme {
                DraggableQuickAddRow(
                    presets = listOf(presetA, presetB, presetC),
                    onDrink = {},
                    onReorder = { it, _ -> capturedOrder = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Long-press chip_100 (A) and drag it to the right past the swap threshold.
        // Drag a full chip width so the swap threshold (itemWidth/2 + spacing/2) is
        // reliably crossed regardless of the device's screen size/density.
        composeTestRule.onNodeWithTag("chip_100").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
            moveBy(Offset(visibleSize.width.toFloat(), 0f))
            up()
        }
        composeTestRule.waitForIdle()

        assertNotNull("onReorder must be called after drag ends", capturedOrder)
        // A moved right; B should now be first.
        assertEquals(presetB, capturedOrder!!.first())
        assertEquals(presetA, capturedOrder[1])
        assertEquals(presetC, capturedOrder.last())
    }

    @Test
    fun movingItemBackRestoresOriginalOrder() {
        // Start with A already at index 1 — simulating a prior reorder.
        val startingOrder = listOf(presetB, presetA, presetC)
        var capturedOrder: List<DrinkSuggestion>? = null

        composeTestRule.setContent {
            DrinkWaterAppTheme {
                DraggableQuickAddRow(
                    presets = startingOrder,
                    onDrink = {},
                    onReorder = { it, _ -> capturedOrder = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Long-press chip_100 (A, currently at index 1) and drag it back left a full
        // chip width so the swap threshold is reliably crossed on any device.
        composeTestRule.onNodeWithTag("chip_100").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
            moveBy(Offset(-visibleSize.width.toFloat(), 0f))
            up()
        }
        composeTestRule.waitForIdle()

        assertNotNull("onReorder must be called after drag ends", capturedOrder)
        // A should be back at index 0.
        assertEquals(presetA, capturedOrder!!.first())
        assertEquals(presetB, capturedOrder[1])
        assertEquals(presetC, capturedOrder.last())
    }

    @Test
    fun movingTwoItemsToDifferentPositionsProducesCorrectFinalOrder() {
        val reorderEvents = mutableListOf<List<DrinkSuggestion>>()

        composeTestRule.setContent {
            DrinkWaterAppTheme {
                DraggableQuickAddRow(
                    presets = listOf(presetA, presetB, presetC),
                    onDrink = {},
                    onReorder = { it, _ -> reorderEvents.add(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // First drag: move A (index 0) right → expected result [B, A, C].
        composeTestRule.onNodeWithTag("chip_100").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
            moveBy(Offset(visibleSize.width.toFloat(), 0f))
            up()
        }
        composeTestRule.waitForIdle()

        // Second drag: move C (now at index 2 in [B, A, C]) left → expected [B, C, A].
        composeTestRule.onNodeWithTag("chip_300").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
            moveBy(Offset(-visibleSize.width.toFloat(), 0f))
            up()
        }
        composeTestRule.waitForIdle()

        assertEquals("onReorder should fire once per drag", 2, reorderEvents.size)

        // After first drag: B leads, A is second, C is last.
        val afterFirstDrag = reorderEvents[0]
        assertEquals(presetB, afterFirstDrag[0])
        assertEquals(presetA, afterFirstDrag[1])
        assertEquals(presetC, afterFirstDrag[2])

        // After second drag: B leads, C is second, A is last.
        val afterSecondDrag = reorderEvents[1]
        assertEquals(presetB, afterSecondDrag[0])
        assertEquals(presetC, afterSecondDrag[1])
        assertEquals(presetA, afterSecondDrag[2])
    }

    @Test
    fun movingItemToEndAndBackRestoresOriginalOrder() {
        var capturedOrder: List<DrinkSuggestion>? = null

        composeTestRule.setContent {
            DrinkWaterAppTheme {
                DraggableQuickAddRow(
                    presets = listOf(presetA, presetB, presetC),
                    onDrink = {},
                    onReorder = { it, _ -> capturedOrder = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        composeTestRule.onNodeWithTag("chip_100").performTouchInput {
            val step = visibleSize.width.toFloat()
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100L)
            moveBy(Offset(step, 0f))    // index 0 → 1
            moveBy(Offset(step, 0f))    // index 1 → 2  (at the end)
            moveBy(Offset(-step, 0f))   // index 2 → 1
            moveBy(Offset(-step, 0f))   // index 1 → 0  (back to start)
            up()
        }
        composeTestRule.waitForIdle()

        assertNotNull("onReorder must be called after the gesture ends", capturedOrder)
        assertEquals(presetA, capturedOrder!![0])
        assertEquals(presetB, capturedOrder[1])
        assertEquals(presetC, capturedOrder[2])
    }
}
