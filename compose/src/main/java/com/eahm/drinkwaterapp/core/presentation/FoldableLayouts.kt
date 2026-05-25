package com.eahm.drinkwaterapp.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Two-column row where the left column is sized to the left display panel in book mode.
 * Falls back to an equal 50/50 split when [bookModeInfo] is null.
 *
 * The nav-rail width is derived internally from the difference between the full window width
 * and the available content width (constraints.maxWidth).
 */
@Composable
fun FoldAwareTwoColumnRow(
    bookModeInfo: BookModeInfo?,
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val screenWidthPx = LocalWindowInfo.current.containerSize.width
    val leftPanelWidthPx = with(density) { bookModeInfo?.leftPanelWidth?.roundToPx() }

    Layout(
        content = { leftContent(); rightContent() },
        modifier = modifier,
    ) { measurables, constraints ->
        val totalWidth = constraints.maxWidth
        val navRailWidthPx = screenWidthPx - totalWidth
        val leftWidthPx = leftPanelWidthPx
            ?.minus(navRailWidthPx)
            ?.coerceIn(0, totalWidth)
            ?: (totalWidth / 2)
        val rightWidthPx = totalWidth - leftWidthPx

        val leftPlaceable = measurables[0].measure(
            constraints.copy(minWidth = 0, maxWidth = leftWidthPx)
        )
        val rightPlaceable = measurables[1].measure(
            constraints.copy(minWidth = 0, maxWidth = rightWidthPx)
        )

        layout(totalWidth, constraints.maxHeight) {
            leftPlaceable.placeRelative(0, 0)
            rightPlaceable.placeRelative(leftWidthPx, 0)
        }
    }
}

/**
 * Two-row column where the top row is sized to the top display panel in tabletop mode.
 * Falls back to an equal 50/50 split when [tableTopInfo] is null.
 *
 * [topInsetHeight] removes the status-bar height from the top panel so the split lands
 * exactly at the physical hinge even when a banner or bottom nav bar is present.
 */
@Composable
fun FoldAwareTwoRowColumn(
    tableTopInfo: TableTopInfo?,
    topInsetHeight: Dp = 0.dp,
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val topPanelHeightPx = with(density) { tableTopInfo?.topPanelHeight?.roundToPx() }
    val topInsetHeightPx = with(density) { topInsetHeight.roundToPx() }

    Layout(
        content = { topContent(); bottomContent() },
        modifier = modifier,
    ) { measurables, constraints ->
        val totalHeight = constraints.maxHeight
        val topHeightPx = topPanelHeightPx
            ?.minus(topInsetHeightPx)
            ?.coerceIn(0, totalHeight)
            ?: (totalHeight / 2)
        val bottomHeightPx = totalHeight - topHeightPx

        val topPlaceable = measurables[0].measure(
            constraints.copy(minHeight = 0, maxHeight = topHeightPx)
        )
        val bottomPlaceable = measurables[1].measure(
            constraints.copy(minHeight = 0, maxHeight = bottomHeightPx)
        )

        layout(constraints.maxWidth, totalHeight) {
            topPlaceable.placeRelative(0, 0)
            bottomPlaceable.placeRelative(0, topHeightPx)
        }
    }
}
