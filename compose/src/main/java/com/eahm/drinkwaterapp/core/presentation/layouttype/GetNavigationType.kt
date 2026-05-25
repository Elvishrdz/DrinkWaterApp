package com.eahm.drinkwaterapp.core.presentation.layouttype

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

@Composable
fun getNavigationType(
    layoutType: LayoutType = getLayoutType(),
): NavigationType {
    return when (layoutType) {
        LayoutType.PORTRAIT,
        LayoutType.TABLE_TOP,
            -> NavigationType.NavigationBar

        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.LANDSCAPE,
        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED
            -> NavigationType.NavigationRail
    }
}

enum class NavigationType {
    NavigationBar,
    NavigationRail,
}

val NavigationType.toNavigationSuite: NavigationSuiteType
    get() = when (this) {
        NavigationType.NavigationBar -> NavigationSuiteType.NavigationBar
        NavigationType.NavigationRail -> NavigationSuiteType.NavigationRail
    }
