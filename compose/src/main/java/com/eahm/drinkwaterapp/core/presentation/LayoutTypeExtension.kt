package com.eahm.drinkwaterapp.core.presentation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
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

            isExpanded ||
                    // when font and screen is big enough, the window value changes from expanded to medium
                    isMedium
                -> {
                when {
                    windowPosture.hingeList.isNotEmpty() -> {
                        if (isBookMode) {
                            // TODO("Add FOLDABLE_BOOK to LayoutType and return it here")
                            LayoutType.FOLDABLE_EXPANDED
                        } else {
                            LayoutType.FOLDABLE_EXPANDED
                        }
                    }

                    // Landscape when width >= height (phone landscape or tablet landscape)
                    window.minWidthDp >= window.minHeightDp -> LayoutType.LANDSCAPE

                    // Tablet in portrait
                    else -> LayoutType.PORTRAIT
                }
            }

            else -> {
                LayoutType.PORTRAIT
            }
        }
    }
}
