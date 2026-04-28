package com.eahm.drinkwaterapp.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DwShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // r-xs
    small = RoundedCornerShape(10.dp),  // r-sm
    medium = RoundedCornerShape(14.dp),  // r-md
    large = RoundedCornerShape(20.dp),  // r-lg
    extraLarge = RoundedCornerShape(28.dp),  // r-xl
)

// Pill — used directly by button components (not a MaterialTheme slot)
val DwPillShape = RoundedCornerShape(999.dp)
