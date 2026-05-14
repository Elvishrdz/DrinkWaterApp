package com.eahm.drinkwaterapp.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Brand ---
val DwWaterDeep = Color(0xFF0E5A82)
val DwWater = Color(0xFF1C8EBF)
val DwWaterLight = Color(0xFF4FB3DD)
val DwTeal = Color(0xFF008577)
val DwTealDark = Color(0xFF00574B)
val DwTealSoft = Color(0xFF2BA89A)
val DwMagenta = Color(0xFFA22C83)
val DwMagentaDark = Color(0xFF902178)
val DwMagentaPop = Color(0xFFD81B60)

// --- Neutrals ---
val DwInk900 = Color(0xFF0E1A20)
val DwInk700 = Color(0xFF1F3640)
val DwInk500 = Color(0xFF4F6772)
val DwInk400 = Color(0xFF6B8290)
val DwInk300 = Color(0xFF94A8B4)
val DwInk200 = Color(0xFFC4D2DA)
val DwInk100 = Color(0xFFE2EBF0)
val DwInk50 = Color(0xFFF2F6F9)
val DwPaper = Color(0xFFFFFFFF)
val DwPaperWarm = Color(0xFFFBFCFD)

// --- Semantic ---
val DwSuccess = Color(0xFF00A051)
val DwWarning = Color(0xFFF4A622)
val DwDanger = Color(0xFFD14343)

// --- Tinted surfaces ---
val DwTintWater = Color(0xFFEAF4F9)
val DwTintMagenta = Color(0xFFFBEFF6)

// --- Gold ---
val DwTintGold = Color(0xFFEFBF04)

// --- Color Schemes ---
internal val DwLightColorScheme = lightColorScheme(
    primary = DwMagenta,
    onPrimary = DwPaper,
    primaryContainer = DwTintMagenta,
    onPrimaryContainer = DwMagentaDark,
    secondary = DwWater,
    onSecondary = DwPaper,
    secondaryContainer = DwTintWater,
    onSecondaryContainer = DwWaterDeep,
    tertiary = DwTeal,
    onTertiary = DwPaper,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = DwTealDark,
    background = DwInk50,
    onBackground = DwInk900,
    surface = DwPaper,
    onSurface = DwInk900,
    surfaceVariant = DwInk50,
    onSurfaceVariant = DwInk500,
    outline = DwInk200,
    outlineVariant = DwInk100,
    error = DwDanger,
    onError = DwPaper,
)

internal val DwDarkColorScheme = darkColorScheme(
    primary = DwMagentaPop,
    onPrimary = DwPaper,
    primaryContainer = DwMagentaDark,
    onPrimaryContainer = DwTintMagenta,
    secondary = DwWaterLight,
    onSecondary = DwWaterDeep,
    secondaryContainer = DwWaterDeep,
    onSecondaryContainer = DwWaterLight,
    tertiary = DwTealSoft,
    onTertiary = DwInk900,
    tertiaryContainer = DwTealDark,
    onTertiaryContainer = Color(0xFFB2DFDB),
    background = DwInk900,
    onBackground = DwInk50,
    surface = DwInk700,
    onSurface = DwInk50,
    surfaceVariant = DwInk700,
    onSurfaceVariant = DwInk300,
    outline = DwInk500,
    outlineVariant = DwInk700,
    error = DwDanger,
    onError = DwPaper,
)
