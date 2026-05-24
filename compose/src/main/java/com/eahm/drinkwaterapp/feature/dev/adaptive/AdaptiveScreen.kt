package com.eahm.drinkwaterapp.feature.dev.adaptive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

@Composable
internal fun AdaptiveScreen(
    onBackClicked: () -> Unit,
) {
    BackHandler(
        onBack = onBackClicked,
    )

    val adaptiveInfo = currentWindowAdaptiveInfo()

    val window = adaptiveInfo.windowSizeClass
    val minWidthDp = window.minWidthDp
    val minHeightDp = window.minHeightDp

    val sizeClassText = "Min Width Dp: $minWidthDp\n" +
            "Min Height Dp: $minHeightDp"

    val widthMedium = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    val widthExpanded = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    val widthLarge = WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
    val widthExtraLarge = WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND

    val heightMedium = WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    val heightExpanded = WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND

    val breakpoint2 = WindowSizeClass.BREAKPOINTS_V2

    val matching = breakpoint2.find {
        val result = it.isAtLeastBreakpoint(window.minWidthDp, window.minHeightDp)
        result
    }

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

    val layoutType = getLayoutType()

    LazyColumn(
        modifier = Modifier.padding(
            WindowInsets.safeDrawing.asPaddingValues()
        ),
    ) {
        item {
            DwPrimaryButton(
                text = "Back to Menu",
                onClick = onBackClicked,
            )
        }

        item {
            Text(
                text = "adaptive type is: $layoutType",
                color = Color.Gray,
            )

            Text(
                text = sizeClassText,
                color = Color.Magenta,
            )

            Text(
                text = "matches with: $matching",
                color = Color.Green,
            )

            Text(
                text = "breakpoints:",
                color = Color.Blue,
            )

            breakpoint2.forEach {
                val color = if (it == matching) Color.Magenta else Color.Blue

                Text(
                    text = "WindowSizeClass | minWidthDp: ${it.minWidthDp}, minHeightDp: ${it.minHeightDp}",
                    color = color,
                )
            }

            Text(
                text = "Constants",
                color = Color.LightGray,
            )

            Text(
                text = "Width medium: $widthMedium",
                color = Color.LightGray,
            )

            Text(
                text = "Width expanded: $widthExpanded",
                color = Color.LightGray,
            )

            Text(
                text = "Width large: $widthLarge",
                color = Color.LightGray,
            )

            Text(
                text = "Width extra large: $widthExtraLarge",
                color = Color.LightGray,
            )

            Text(
                text = "Height medium: $heightMedium",
                color = Color.Gray,
            )

            Text(
                text = "Height expanded: $heightExpanded",
                color = Color.Gray,
            )


            Text(
                text = "isExpanded: $isExpanded",
                color = Color.Cyan,
            )

            Text(
                text = "isMedium: $isMedium",
                color = Color.Cyan,
            )
        }
    }
}
