package com.eahm.drinkwaterapp.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun DrinkWaterAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // off by default — always use brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DwDarkColorScheme
        else -> DwLightColorScheme
    }

    val view = LocalView.current
    val context = view.context

    // check is activity to not crash in GoalReachedWidget.kt overlay
    if (!view.isInEditMode && context is Activity) {
        SideEffect {
            val window = context.window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalDwSpacing provides DwSpacing(),
        LocalDwExtendedTypography provides DwExtendedTypography(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DwTypography,
            shapes = DwShapes,
            content = content,
        )
    }
}
