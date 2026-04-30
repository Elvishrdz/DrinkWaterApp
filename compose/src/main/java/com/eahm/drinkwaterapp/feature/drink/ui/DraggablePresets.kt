package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.core.data.datastore.DrinkPreset

private const val DRAG_SCALE = 1.07f
private const val DRAG_ELEVATION = 20f
private const val GRID_COLUMNS = 3

// ── Row ──────────────────────────────────────────────────────────────────────
//
// Drop-in for the preset chips in QuickAddRow. Accepts a [trailingContent]
// slot (for the "More" button) so all items share the same weight-based Row.
//
// Live-swap mechanism:
//   - Long press → haptic + item scales up with shadow
//   - Drag horizontally → when the dragged item's center crosses a neighbor's
//     midpoint, the two swap instantly (item stays under the finger)
//   - Release → onReorder called with the new order
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DraggableQuickAddRow(
    presets: List<DrinkPreset>,
    onDrink: (Float) -> Unit,
    onReorder: (List<DrinkPreset>) -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (Modifier) -> Unit = {},
) {
    var items by remember { mutableStateOf(presets) }
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    val isReorderingItem by derivedStateOf { dragIndex != null }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var itemWidth by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spacingPx = remember(density) { with(density) { 8.dp.toPx() } }
    val haptic = LocalHapticFeedback.current

    // Sync from ViewModel when not dragging
    LaunchedEffect(presets) {
        if (dragIndex == null) {
            items = listOf()
            items = presets
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEachIndexed { index, preset ->
            // key() ensures Compose moves this composable (preserving its pointerInput
            // gesture state) instead of destroying and recreating it on every swap.
            key(preset.amountMl) {
                val isDragging = dragIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) DRAG_SCALE else 1f,
                    animationSpec = spring(),
                    label = "row_scale_$index",
                )
                val elevation by animateFloatAsState(
                    targetValue = if (isDragging) DRAG_ELEVATION else 0f,
                    label = "row_elev_$index",
                )

                WaterChip(
                    amount = preset.amountMl,
                    onClick = {
                        if (dragIndex == null) onDrink(preset.amountMl)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { if (it.width > 0) itemWidth = it.width.toFloat() }
                        .zIndex(if (isDragging) 10f else 0f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            shadowElevation = elevation
                            translationX = if (isDragging) dragOffsetX else 0f
                        }
                        .pointerInput(preset.amountMl) {
                            // Snapshot the list at drag start so we can restore on cancel.
                            var snapshot: List<DrinkPreset> = emptyList()

                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    snapshot = items
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dragIndex = index
                                    dragOffsetX = 0f
                                },
                                onDrag = { _, delta ->
                                    dragOffsetX += delta.x
                                    val currentDragIndex = dragIndex
                                        ?: return@detectDragGesturesAfterLongPress

                                    val currentItemWidth = itemWidth.takeIf { it > 0f }
                                        ?: return@detectDragGesturesAfterLongPress

                                    val threshold = currentItemWidth / 2f + spacingPx / 2f

                                    when {
                                        dragOffsetX > threshold && currentDragIndex < items.lastIndex -> {
                                            val newList = items.toMutableList()
                                            newList.add(
                                                currentDragIndex + 1,
                                                newList.removeAt(currentDragIndex)
                                            )
                                            items = newList
                                            dragIndex = currentDragIndex + 1
                                            dragOffsetX -= currentItemWidth + spacingPx
                                        }

                                        dragOffsetX < -threshold && currentDragIndex > 0 -> {
                                            val newList = items.toMutableList()
                                            newList.add(
                                                currentDragIndex - 1,
                                                newList.removeAt(currentDragIndex)
                                            )
                                            items = newList
                                            dragIndex = currentDragIndex - 1
                                            dragOffsetX += currentItemWidth + spacingPx
                                        }
                                    }
                                },
                                onDragEnd = {
                                    onReorder(items)
                                    dragIndex = null
                                    dragOffsetX = 0f
                                },
                                onDragCancel = {
                                    items = snapshot
                                    dragIndex = null
                                    dragOffsetX = 0f
                                },
                            )
                        },
                )
            }
        }

        // Non-draggable trailing slot (the "+ More" button)
        trailingContent(Modifier.weight(1f))
    }
}

// ── Grid ─────────────────────────────────────────────────────────────────────
//
// Replaces LazyVerticalGrid in the bottom sheet. Same live-swap mechanic
// extended to 2D: horizontal swaps fire first (same row), then vertical
// (adjacent row), never both in the same drag event.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DraggablePresetGrid(
    presets: List<DrinkPreset>,
    onDrink: (Float) -> Unit,
    onReorder: (List<DrinkPreset>) -> Unit,
    columns: Int = GRID_COLUMNS,
    modifier: Modifier = Modifier,
) {
    var items by remember { mutableStateOf(presets) }
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var itemWidth by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val hSpacingPx = remember(density) { with(density) { 7.dp.toPx() } }
    val vSpacingPx = remember(density) { with(density) { 7.dp.toPx() } }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(presets) {
        if (dragIndex == null) items = presets
    }

    val rowCount = (items.size + columns - 1) / columns

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        for (rowIdx in 0 until rowCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                for (colIdx in 0 until columns) {
                    val flatIdx = rowIdx * columns + colIdx

                    if (flatIdx < items.size) {
                        val preset = items[flatIdx]

                        key(preset.amountMl) {
                            val isDragging = dragIndex == flatIdx
                            val scale by animateFloatAsState(
                                targetValue = if (isDragging) DRAG_SCALE else 1f,
                                animationSpec = spring(),
                                label = "grid_scale_$flatIdx",
                            )
                            val elevation by animateFloatAsState(
                                targetValue = if (isDragging) DRAG_ELEVATION else 0f,
                                label = "grid_elev_$flatIdx",
                            )

                            WaterChip(
                                amount = preset.amountMl,
                                onClick = { if (dragIndex == null) onDrink(preset.amountMl) },
                                modifier = Modifier
                                    .weight(1f)
                                    .onSizeChanged {
                                        if (it.width > 0) itemWidth = it.width.toFloat()
                                        if (it.height > 0) itemHeight = it.height.toFloat()
                                    }
                                    .zIndex(if (isDragging) 10f else 0f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation
                                        translationX = if (isDragging) dragOffset.x else 0f
                                        translationY = if (isDragging) dragOffset.y else 0f
                                    }
                                    .pointerInput(preset.amountMl) {
                                        var snapshot: List<DrinkPreset> = emptyList()

                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                snapshot = items
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                dragIndex = flatIdx
                                                dragOffset = Offset.Zero
                                            },
                                            onDrag = { _, delta ->
                                                dragOffset += delta
                                                val cur = dragIndex
                                                    ?: return@detectDragGesturesAfterLongPress
                                                val iw = itemWidth.takeIf { it > 0f }
                                                    ?: return@detectDragGesturesAfterLongPress
                                                val ih = itemHeight.takeIf { it > 0f }
                                                    ?: return@detectDragGesturesAfterLongPress

                                                val hThreshold = iw / 2f + hSpacingPx / 2f
                                                val vThreshold = ih / 2f + vSpacingPx / 2f
                                                val curCol = cur % columns

                                                // One swap per drag event — horizontal first
                                                when {
                                                    dragOffset.x > hThreshold
                                                            && curCol < columns - 1
                                                            && cur + 1 < items.size -> {
                                                        val newList = items.toMutableList()
                                                        newList.add(cur + 1, newList.removeAt(cur))
                                                        items = newList
                                                        dragIndex = cur + 1
                                                        dragOffset = dragOffset.copy(
                                                            x = dragOffset.x - iw - hSpacingPx
                                                        )
                                                    }

                                                    dragOffset.x < -hThreshold && curCol > 0 -> {
                                                        val newList = items.toMutableList()
                                                        newList.add(cur - 1, newList.removeAt(cur))
                                                        items = newList
                                                        dragIndex = cur - 1
                                                        dragOffset = dragOffset.copy(
                                                            x = dragOffset.x + iw + hSpacingPx
                                                        )
                                                    }

                                                    dragOffset.y > vThreshold
                                                            && cur + columns < items.size -> {
                                                        val newList = items.toMutableList()
                                                        newList.add(
                                                            cur + columns,
                                                            newList.removeAt(cur)
                                                        )
                                                        items = newList
                                                        dragIndex = cur + columns
                                                        dragOffset = dragOffset.copy(
                                                            y = dragOffset.y - ih - vSpacingPx
                                                        )
                                                    }

                                                    dragOffset.y < -vThreshold
                                                            && cur - columns >= 0 -> {
                                                        val newList = items.toMutableList()
                                                        newList.add(
                                                            cur - columns,
                                                            newList.removeAt(cur)
                                                        )
                                                        items = newList
                                                        dragIndex = cur - columns
                                                        dragOffset = dragOffset.copy(
                                                            y = dragOffset.y + ih + vSpacingPx
                                                        )
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                onReorder(items)
                                                dragIndex = null
                                                dragOffset = Offset.Zero
                                            },
                                            onDragCancel = {
                                                items = snapshot
                                                dragIndex = null
                                                dragOffset = Offset.Zero
                                            },
                                        )
                                    },
                            )
                        }
                    } else {
                        // Pad last row so items keep equal widths
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
