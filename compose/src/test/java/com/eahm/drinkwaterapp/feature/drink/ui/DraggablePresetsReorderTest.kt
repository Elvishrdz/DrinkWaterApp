package com.eahm.drinkwaterapp.feature.drink.ui

import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [applyHorizontalSwap] — the pure list-reorder logic extracted
 * from [DraggableQuickAddRow].
 *
 * These run on the JVM with no Android or Compose dependencies.
 */
class DraggablePresetsReorderTest {

    private val presetA = DrinkPreset(order = 1, amountMl = 100f)
    private val presetB = DrinkPreset(order = 2, amountMl = 200f)
    private val presetC = DrinkPreset(order = 3, amountMl = 300f)

    // Use fixed dimensions so threshold is predictable.
    private val itemWidth = 100f
    private val spacingPx = 8f

    // threshold = itemWidth / 2 + spacingPx / 2  →  54f
    private val threshold = itemWidth / 2f + spacingPx / 2f

    @Test
    fun `moving item right past threshold swaps it with its right neighbour`() {
        val initial = listOf(presetA, presetB, presetC)

        val result = applyHorizontalSwap(
            list = initial,
            dragIndex = 0,
            dragOffsetX = threshold + 1f,   // just over the threshold
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNotNull(result)
        assertEquals(listOf(presetB, presetA, presetC), result!!.presets)
        assertEquals(1, result.newIndex)
        // Offset should be reduced by one cell width + spacing
        assertEquals(threshold + 1f - itemWidth - spacingPx, result.newOffsetX, 0.01f)
    }

    @Test
    fun `moving item left past threshold swaps it with its left neighbour`() {
        val initial = listOf(presetA, presetB, presetC)

        val result = applyHorizontalSwap(
            list = initial,
            dragIndex = 2,
            dragOffsetX = -(threshold + 1f),
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNotNull(result)
        assertEquals(listOf(presetA, presetC, presetB), result!!.presets)
        assertEquals(1, result.newIndex)
        assertEquals(-(threshold + 1f) + itemWidth + spacingPx, result.newOffsetX, 0.01f)
    }

    @Test
    fun `no swap occurs when offset is below threshold`() {
        val initial = listOf(presetA, presetB, presetC)

        val result = applyHorizontalSwap(
            list = initial,
            dragIndex = 0,
            dragOffsetX = threshold - 1f,   // just under the threshold
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNull(result)
    }

    @Test
    fun `moving item back to original position restores the initial order`() {
        // Simulate: A was dragged from index 0 to index 1, giving [B, A, C].
        val afterFirstSwap = listOf(presetB, presetA, presetC)

        // Now drag A (at index 1) back left.
        val result = applyHorizontalSwap(
            list = afterFirstSwap,
            dragIndex = 1,
            dragOffsetX = -(threshold + 1f),
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNotNull(result)
        assertEquals(listOf(presetA, presetB, presetC), result!!.presets)
        assertEquals(0, result.newIndex)
    }

    @Test
    fun `moving rightmost item back to its original position restores order`() {
        // [A, C, B] — B was swapped right once; drag B back left.
        val reordered = listOf(presetA, presetC, presetB)

        val result = applyHorizontalSwap(
            list = reordered,
            dragIndex = 2,
            dragOffsetX = -(threshold + 1f),
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNotNull(result)
        assertEquals(listOf(presetA, presetB, presetC), result!!.presets)
        assertEquals(1, result.newIndex)
    }

    @Test
    fun `moving two items to different positions yields correct final order`() {
        val initial = listOf(presetA, presetB, presetC)

        // First swap: move A (index 0) right → [B, A, C], dragIndex = 1
        val firstSwap = applyHorizontalSwap(
            list = initial,
            dragIndex = 0,
            dragOffsetX = threshold + 1f,
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )
        assertNotNull(firstSwap)
        assertEquals(listOf(presetB, presetA, presetC), firstSwap!!.presets)
        assertEquals(1, firstSwap.newIndex)

        // Second swap (independent drag): move C (index 2) left → [B, C, A]
        val secondSwap = applyHorizontalSwap(
            list = firstSwap.presets,
            dragIndex = 2,
            dragOffsetX = -(threshold + 1f),
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )
        assertNotNull(secondSwap)
        assertEquals(listOf(presetB, presetC, presetA), secondSwap!!.presets)
        assertEquals(1, secondSwap.newIndex)
    }

    @Test
    fun `cannot swap the first item further left`() {
        val initial = listOf(presetA, presetB, presetC)

        val result = applyHorizontalSwap(
            list = initial,
            dragIndex = 0,              // already at the leftmost position
            dragOffsetX = -(threshold + 1f),
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNull("first item cannot move left", result)
    }

    @Test
    fun `cannot swap the last item further right`() {
        val initial = listOf(presetA, presetB, presetC)

        val result = applyHorizontalSwap(
            list = initial,
            dragIndex = 2,              // already at the rightmost position
            dragOffsetX = threshold + 1f,
            itemWidth = itemWidth,
            spacingPx = spacingPx,
        )

        assertNull("last item cannot move right", result)
    }
}
