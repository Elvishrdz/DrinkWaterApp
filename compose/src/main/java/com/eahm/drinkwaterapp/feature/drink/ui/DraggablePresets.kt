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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight

private const val DRAG_SCALE = 1.3f
private const val DRAG_ELEVATION = 0f // FIXME: elevation cutout
private const val DRAG_Z_INDEX = 5f
private const val GRID_COLUMNS = 3

private val horizontalSpacing = 8.dp
private val gridSpacing = 7.dp

internal data class SwapResult(
    val presets: List<DrinkSuggestion>,
    val newIndex: Int,
    val newOffsetX: Float,
)

/**
 * Returns the new list, drag index, and adjusted offset after a horizontal
 * threshold-based swap, or null if no swap threshold was crossed.
 */
internal fun applyHorizontalSwap(
    list: List<DrinkSuggestion>,
    dragIndex: Int,
    dragOffsetX: Float,
    itemWidth: Float,
    spacingPx: Float,
): SwapResult? {
    val threshold = itemWidth / 2f + spacingPx / 2f
    return when {
        dragOffsetX > threshold && dragIndex < list.lastIndex -> {
            val newList = list.toMutableList()
            newList.add(dragIndex + 1, newList.removeAt(dragIndex))
            SwapResult(newList, dragIndex + 1, dragOffsetX - itemWidth - spacingPx)
        }

        dragOffsetX < -threshold && dragIndex > 0 -> {
            val newList = list.toMutableList()
            newList.add(dragIndex - 1, newList.removeAt(dragIndex))
            SwapResult(newList, dragIndex - 1, dragOffsetX + itemWidth + spacingPx)
        }

        else -> null
    }
}

@Composable
fun DraggableQuickAddRow(
    presets: List<DrinkSuggestion>,
    onDrink: (Float) -> Unit,
    onReorder: (List<DrinkSuggestion>, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (Modifier) -> Unit = {},
) {
    var currentPresets by remember { mutableStateOf(presets) }
    var previousPresets by remember { mutableStateOf(emptyList<DrinkSuggestion>()) }

    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    val isDraggingItem by remember { derivedStateOf { draggedItemIndex != null } }

    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    var itemWidth by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val spacingPx = remember(density) { with(density) { horizontalSpacing.toPx() } }
    val haptic = LocalHapticFeedback.current

    // Sync from ViewModel when not dragging
    LaunchedEffect(presets) {
        if (draggedItemIndex == null) {
            currentPresets = presets
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            // required to draw child over other elements
            .zIndex(5f),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    ) {
        currentPresets.forEachIndexed { index, preset ->
            // key() ensures Compose moves this composable (preserving its pointerInput
            // gesture state) instead of destroying and recreating it on every swap.
            key(preset.amountMl) {
                // rememberUpdatedState keeps this in sync with the current index on
                // every recomposition without restarting the pointerInput coroutine.
                // Without this, the gesture coroutine captures the index from when
                // it first launched, so after a reorder onDragStart points at the
                // wrong slot (the item's original position, not its current one).
                val currentIndex by rememberUpdatedState(index)
                val itemBeingDragged = draggedItemIndex == index
                var itemPosX by remember { mutableStateOf(0f) }
                var itemPosY by remember { mutableStateOf(0f) }

                val scale by animateFloatAsState(
                    targetValue = if (itemBeingDragged) DRAG_SCALE else 1f,
                    animationSpec = spring(),
                    label = "preset_scale_$index",
                )

                val elevation by animateFloatAsState(
                    targetValue = if (itemBeingDragged) DRAG_ELEVATION else 0f,
                    label = "preset_elev_$index",
                )

                WaterChip(
                    amount = preset.amountMl,
                    onClick = {
                        if (isDraggingItem) return@WaterChip

                        onDrink(preset.amountMl)
                    },
                    animateOnClick = !isDraggingItem,
                    modifier = Modifier
                        .testTag("chip_${preset.amountMl.toInt()}")
                        .weight(1f)
                        .onSizeChanged { size ->
                            if (size.width > 0) itemWidth = size.width.toFloat()
                        }
                        .zIndex(if (itemBeingDragged) DRAG_Z_INDEX else 0f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            shadowElevation = elevation

                            val newPosX = if (itemBeingDragged) dragOffsetX else 0f
                            translationX = newPosX
                            itemPosX = translationX

                            val newPosY = if (itemBeingDragged) dragOffsetY else 0f
                            translationY = newPosY
                            itemPosY = translationY
                        }
                        .pointerInput(preset.amountMl) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    previousPresets = currentPresets
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    draggedItemIndex = currentIndex
                                    dragOffsetX = itemPosX
                                    dragOffsetY = itemPosY
                                },
                                onDrag = { _, delta ->
                                    val currentDragIndex = draggedItemIndex
                                        ?: return@detectDragGesturesAfterLongPress
                                    val currentItemWidth = itemWidth.takeIf { it > 0f }
                                        ?: return@detectDragGesturesAfterLongPress

                                    dragOffsetX += delta.x
                                    dragOffsetY += delta.y

                                    applyHorizontalSwap(
                                        list = currentPresets,
                                        dragIndex = currentDragIndex,
                                        dragOffsetX = dragOffsetX,
                                        itemWidth = currentItemWidth,
                                        spacingPx = spacingPx,
                                    )?.let { result ->
                                        currentPresets = result.presets
                                        draggedItemIndex = result.newIndex
                                        dragOffsetX = result.newOffsetX
                                    }
                                },
                                onDragEnd = {
                                    onReorder(currentPresets, true)
                                    draggedItemIndex = null
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    currentPresets = previousPresets
                                    draggedItemIndex = null
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                },
                            )
                        },
                )
            }
        }

        trailingContent(Modifier.weight(1f))
    }
}

// ── Grid ─────────────────────────────────────────────────────────────────────
//
// Replaces LazyVerticalGrid in the bottom sheet. Same live-swap mechanic
// extended to 2D: horizontal swaps fire first (same row), then vertical
// (adjacent row), never both in the same drag event.
//
// IMPORTANT: gesture detection lives on the Column, NOT on individual items.
// With per-item pointerInput, a vertical swap moves a key() node from one Row
// to another — Compose destroys and recreates it, which cancels the active
// gesture and snaps the item back. A single pointerInput on the Column
// survives all swaps intact. Items are purely visual: they read grid-level
// state to render scale, elevation and translation.
//
// A NestedScrollConnection also consumes all scroll while dragging so the
// bottom sheet's anchoredDraggable cannot steal the gesture.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DraggablePresetGrid(
    presets: List<DrinkSuggestion>,
    onDrink: (Float) -> Unit,
    onReorder: (List<DrinkSuggestion>) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (DrinkSuggestion) -> Unit = {},
    onDraggingChanged: (Boolean) -> Unit = {},
    onDeleteHover: (Boolean) -> Unit = {},
    columns: Int = GRID_COLUMNS,
) {
    var currentPresets by remember { mutableStateOf(presets) }
    var previousPresets by remember { mutableStateOf(emptyList<DrinkSuggestion>()) }

    // Hold the state object explicitly so NestedScrollConnection can read the
    // current value without capturing a stale reference.
    val dragIndexState = remember { mutableStateOf<Int?>(null) }
    var draggedItemIndex by dragIndexState
    val isDraggingItem by remember { derivedStateOf { draggedItemIndex != null } }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var itemWidth by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val hSpacingPx = remember(density) { with(density) { gridSpacing.toPx() } }
    val vSpacingPx = remember(density) { with(density) { gridSpacing.toPx() } }
    val haptic = LocalHapticFeedback.current

    var gridHeight by remember { mutableFloatStateOf(0f) }
    var isOverDeleteZone by remember { mutableStateOf(false) }

    // Sync from ViewModel when not dragging
    LaunchedEffect(presets) {
        if (draggedItemIndex == null) currentPresets = presets
    }

    LaunchedEffect(isDraggingItem) {
        onDraggingChanged(isDraggingItem)
    }

    LaunchedEffect(isOverDeleteZone) {
        onDeleteHover(isOverDeleteZone)
    }

    // While a drag is active, eat every scroll event before it reaches the
    // parent bottom sheet. Without this the sheet moves and fires onDragCancel.
    val nestedScrollConnection = remember(dragIndexState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (dragIndexState.value != null) available else Offset.Zero
        }
    }

    val rowCount = (currentPresets.size + columns - 1) / columns

    Column(
        modifier = modifier
            .zIndex(1f)
            .onSizeChanged { gridHeight = it.height.toFloat() }
            .nestedScroll(nestedScrollConnection)
            // Single grid-level gesture — survives cross-row swaps intact.
            // onDragStart finds the hit cell from the touch offset and item
            // dimensions; subsequent onDrag events update grid-level state.
            .pointerInput(columns) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val iw = itemWidth.takeIf { it > 0f }
                            ?: return@detectDragGesturesAfterLongPress
                        val ih = itemHeight.takeIf { it > 0f }
                            ?: return@detectDragGesturesAfterLongPress

                        val col = (offset.x / (iw + hSpacingPx))
                            .toInt().coerceIn(0, columns - 1)
                        val row = (offset.y / (ih + vSpacingPx))
                            .toInt().coerceAtLeast(0)
                        val idx = row * columns + col

                        if (idx >= currentPresets.size) return@detectDragGesturesAfterLongPress

                        previousPresets = currentPresets
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        draggedItemIndex = idx
                        dragOffset = Offset.Zero
                    },
                    onDrag = { _, delta ->
                        val cur = draggedItemIndex
                            ?: return@detectDragGesturesAfterLongPress
                        val iw = itemWidth.takeIf { it > 0f }
                            ?: return@detectDragGesturesAfterLongPress
                        val ih = itemHeight.takeIf { it > 0f }
                            ?: return@detectDragGesturesAfterLongPress

                        dragOffset += delta

                        // Detect if the dragged item's visual center has moved below the grid
                        val draggedRow = cur / columns
                        val itemCenterY = draggedRow * (ih + vSpacingPx) + ih / 2f
                        isOverDeleteZone = itemCenterY + dragOffset.y > gridHeight

                        if (!isOverDeleteZone) {
                            val hThreshold = iw / 2f + hSpacingPx / 2f
                            val vThreshold = ih / 2f + vSpacingPx / 2f
                            val curCol = cur % columns

                            // One swap per drag event — horizontal first
                            when {
                                dragOffset.x > hThreshold
                                        && curCol < columns - 1
                                        && cur + 1 < currentPresets.size -> {
                                    val newList = currentPresets.toMutableList()
                                    newList.add(cur + 1, newList.removeAt(cur))
                                    currentPresets = newList
                                    draggedItemIndex = cur + 1
                                    dragOffset = dragOffset.copy(x = dragOffset.x - iw - hSpacingPx)
                                }

                                dragOffset.x < -hThreshold && curCol > 0 -> {
                                    val newList = currentPresets.toMutableList()
                                    newList.add(cur - 1, newList.removeAt(cur))
                                    currentPresets = newList
                                    draggedItemIndex = cur - 1
                                    dragOffset = dragOffset.copy(x = dragOffset.x + iw + hSpacingPx)
                                }

                                dragOffset.y > vThreshold
                                        && cur + columns < currentPresets.size -> {
                                    val newList = currentPresets.toMutableList()
                                    newList.add(cur + columns, newList.removeAt(cur))
                                    currentPresets = newList
                                    draggedItemIndex = cur + columns
                                    dragOffset = dragOffset.copy(y = dragOffset.y - ih - vSpacingPx)
                                }

                                dragOffset.y < -vThreshold
                                        && cur - columns >= 0 -> {
                                    val newList = currentPresets.toMutableList()
                                    newList.add(cur - columns, newList.removeAt(cur))
                                    currentPresets = newList
                                    draggedItemIndex = cur - columns
                                    dragOffset = dragOffset.copy(y = dragOffset.y + ih + vSpacingPx)
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        val cur = draggedItemIndex
                        val overDelete = isOverDeleteZone
                        draggedItemIndex = null
                        dragOffset = Offset.Zero
                        isOverDeleteZone = false

                        if (cur != null && overDelete) {
                            val deletedPreset = currentPresets[cur]
                            currentPresets =
                                currentPresets.toMutableList().also { it.removeAt(cur) }
                            onDelete(deletedPreset)
                        } else {
                            onReorder(currentPresets)
                        }
                    },
                    onDragCancel = {
                        currentPresets = previousPresets
                        draggedItemIndex = null
                        dragOffset = Offset.Zero
                        isOverDeleteZone = false
                    },
                )
            },
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
    ) {
        val quickPresetBorderColor = DwWaterLight.copy(alpha = 0.5f)

        for (rowIdx in 0 until rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (rowIdx == 0) {
                            Modifier.drawWithContent {
                                val radius = CornerRadius(14.dp.toPx())
                                drawRoundRect(
                                    color = quickPresetBorderColor.copy(alpha = 0.3f),
                                    cornerRadius = radius,
                                )
                                drawContent()
                                drawRoundRect(
                                    color = quickPresetBorderColor,
                                    cornerRadius = radius,
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            ) {
                for (colIdx in 0 until columns) {
                    val flatIdx = rowIdx * columns + colIdx

                    if (flatIdx < currentPresets.size) {
                        val preset = currentPresets[flatIdx]

                        // key() preserves animation state for same-row swaps.
                        // Cross-row swaps recreate the node, but that is fine
                        // now that gesture state lives on the Column.
                        key(preset.amountMl) {
                            val itemBeingDragged = draggedItemIndex == flatIdx

                            val scale by animateFloatAsState(
                                targetValue = if (itemBeingDragged) DRAG_SCALE else 1f,
                                animationSpec = spring(),
                                label = "grid_scale_$flatIdx",
                            )
                            val elevation by animateFloatAsState(
                                targetValue = if (itemBeingDragged) DRAG_ELEVATION else 0f,
                                label = "grid_elev_$flatIdx",
                            )

                            WaterChip(
                                amount = preset.amountMl,
                                onClick = {
                                    if (isDraggingItem) return@WaterChip
                                    onDrink(preset.amountMl)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onSizeChanged {
                                        if (it.width > 0) itemWidth = it.width.toFloat()
                                        if (it.height > 0) itemHeight = it.height.toFloat()
                                    }
                                    .zIndex(if (itemBeingDragged) DRAG_Z_INDEX else 0f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        shadowElevation = elevation
                                        translationX = if (itemBeingDragged) dragOffset.x else 0f
                                        translationY = if (itemBeingDragged) dragOffset.y else 0f
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
