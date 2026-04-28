package com.eahm.drinkwaterapp.core.presentation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

@Composable
fun getLayoutType(): LayoutType {
    val adaptiveInfo = currentWindowAdaptiveInfo()

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
                // Hinge list is empty when phone is in landscape
                val isLandscape = windowPosture.hingeList.isEmpty()

                if (isLandscape) {
                    LayoutType.LANDSCAPE

                } else {
                    LayoutType.FOLDABLE_EXPANDED
                }
            }

            else -> {
                LayoutType.PORTRAIT
            }
        }
    }
}
