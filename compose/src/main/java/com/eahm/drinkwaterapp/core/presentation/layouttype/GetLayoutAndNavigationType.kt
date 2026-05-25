package com.eahm.drinkwaterapp.core.presentation.layouttype

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

/**
 * Convenient class to calculate layout and navigation once
 */
@Composable
fun getLayoutAndNavigationType(): LayoutNavigationType {
    val layoutType = getLayoutType()
    val navigationType = getNavigationType(layoutType)

    return LayoutNavigationType(
        layoutType = layoutType,
        navigationType = navigationType,
    )
}

@Immutable
data class LayoutNavigationType(
    val layoutType: LayoutType,
    val navigationType: NavigationType,
)
