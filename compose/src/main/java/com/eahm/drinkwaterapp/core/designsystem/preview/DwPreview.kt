package com.eahm.drinkwaterapp.core.designsystem.preview

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

/**
 * Renders a composable in both light and dark variants inside [DrinkWaterAppTheme].
 *
 * Usage:
 * ```
 * @DwPreview
 * @Composable
 * private fun MyScreenPreview() {
 *     DwThemePreview { MyScreen() }
 * }
 * ```
 */
@Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO or UI_MODE_TYPE_NORMAL)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
annotation class DwPreview

/**
 * Wraps [content] in [DrinkWaterAppTheme] + a full-background [Surface].
 * Intended for use inside [DwPreview]-annotated functions.
 */
@Composable
fun DwThemePreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    DrinkWaterAppTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
