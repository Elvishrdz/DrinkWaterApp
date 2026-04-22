package com.eahm.drinkwaterapp.core.presentation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun getLayoutType(): LayoutType {
    val adaptiveInfo = currentWindowAdaptiveInfo()

    return with(adaptiveInfo) {
        when {
            windowPosture.isTabletop -> {
                LayoutType.TABLE_TOP
            }

            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
                    // when font and screen is big enough, the window value changes from expanded to medium
                    windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
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
