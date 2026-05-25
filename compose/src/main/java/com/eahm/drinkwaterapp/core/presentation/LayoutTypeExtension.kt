package com.eahm.drinkwaterapp.core.presentation

import android.annotation.SuppressLint
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.map

@Composable
fun getLayoutType(): LayoutType {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val context = LocalContext.current

    // Book mode: foldable half-open with vertical hinge (like holding a book).
    // Posture.isTabletop already covers HALF_OPENED + HORIZONTAL (laptop/tabletop posture),
    // so we use FoldingFeature directly to detect HALF_OPENED + VERTICAL.
    val isBookMode by remember(context) {
        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(context)
            .map { layoutInfo ->
                layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .any {
                        it.orientation == FoldingFeature.Orientation.VERTICAL &&
                                it.state == FoldingFeature.State.HALF_OPENED
                    }
            }
    }.collectAsStateWithLifecycle(false)

    val window = adaptiveInfo.windowSizeClass

    val isExpanded = when {
        window.minWidthDp < WIDTH_DP_MEDIUM_LOWER_BOUND -> false
        window.minWidthDp < WIDTH_DP_EXPANDED_LOWER_BOUND -> false
        else -> true
    }

    val isMedium = when {
        window.minWidthDp < WIDTH_DP_MEDIUM_LOWER_BOUND -> false
        window.minWidthDp < WIDTH_DP_EXPANDED_LOWER_BOUND -> true
        else -> false
    }

    return with(adaptiveInfo) {
        when {
            windowPosture.isTabletop -> {
                // TODO: extend to support hinges and separated screen devices
                LayoutType.TABLE_TOP
            }

            // Foldable with hinge: book mode or fully expanded
            windowPosture.hingeList.isNotEmpty() && (isExpanded || isMedium) -> {
                if (isBookMode) {
                    LayoutType.FOLDABLE_BOOK
                } else {
                    LayoutType.FOLDABLE_EXPANDED
                }
            }

            // Landscape: phone landscape or tablet landscape (width >= height)
            (isExpanded || isMedium) && window.minWidthDp >= window.minHeightDp -> {
                LayoutType.LANDSCAPE
            }

            // Portrait: phone portrait (compact width) or tablet portrait (medium/expanded width).
            else -> {
                val isTabletPortrait = isExpanded || isMedium

                if (isTabletPortrait) {
                    LayoutType.PORTRAIT_EXPANDED
                } else {
                    LayoutType.PORTRAIT
                }
            }
        }
    }
}

/**
 * TODO: WIP
 * Returns hinge geometry when the device is in book mode (foldable half-open with a
 * vertical hinge), or null in any other layout. Call this alongside [getLayoutType] when
 * you need to constrain content to a single panel or avoid the hinge area.
 *
 * Usage:
 * ```
 * val bookModeInfo = rememberBookModeInfo()
 * val maxWidth = bookModeInfo?.leftPanelWidth ?: Dp.Unspecified
 * ```
 */
// TODO: table top also can be affected. change name from book mode.
@SuppressLint("RestrictedApi")
@Composable
private fun rememberBookModeInfo(): BookModeInfo? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val windowWidthPx = LocalWindowInfo.current.containerSize.width

    val initial = WindowLayoutInfo(emptyList())

    val windowLayoutInfo by remember(context) {
        WindowInfoTracker.getOrCreate(context)
            .windowLayoutInfo(context)
    }.collectAsStateWithLifecycle(initial)

    val bookFeature = windowLayoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull {
            it.orientation == FoldingFeature.Orientation.VERTICAL &&
                    it.state == FoldingFeature.State.HALF_OPENED
        } ?: return null

    return with(density) {
        BookModeInfo(
            leftPanelWidth = bookFeature.bounds.left.toDp(),
            rightPanelWidth = (windowWidthPx - bookFeature.bounds.right).toDp(),
            hingeCenter = ((bookFeature.bounds.left + bookFeature.bounds.right) / 2).toDp(),
            hingeWidth = (bookFeature.bounds.right - bookFeature.bounds.left).toDp(),
            isSeparating = bookFeature.isSeparating,
        )
    }
}

/**
 * Geometry of the hinge area when a foldable device is in book mode
 * (half-open with a vertical hinge). Use this to keep content within
 * a single panel and avoid drawing into the hinge/occluded area.
 *
 * Coordinate reference (full unfolded width):
 *
 *   |<-- leftPanelWidth -->|<-- hingeWidth -->|<-- rightPanelWidth -->|
 *   0                  hingeStart         hingeEnd               windowWidth
 *                           |<- hingeCenter ->|
 */
private data class BookModeInfo(
    /** Usable width of the left panel (from the left edge to the hinge start). */
    val leftPanelWidth: Dp,
    /** Usable width of the right panel (from the hinge end to the right edge). */
    val rightPanelWidth: Dp,
    /** Center X position of the hinge. Useful as a split line or max-width anchor. */
    val hingeCenter: Dp,
    /** Width of the hinge area. Zero on flexible/transparent folds. */
    val hingeWidth: Dp,
    /**
     * True when the hinge is a physical opaque gap (content drawn here is not visible).
     * False on flexible displays where the fold is a soft crease with no black area.
     */
    val isSeparating: Boolean,
)
