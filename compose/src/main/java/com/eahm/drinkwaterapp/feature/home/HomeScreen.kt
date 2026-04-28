package com.eahm.drinkwaterapp.feature.home

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.feature.home.settings.SettingsScreen
import com.eahm.drinkwaterapp.feature.home.stats.StatsScreen
import com.eahm.drinkwaterapp.feature.home.today.TodayScreen
import com.eahm.drinkwaterapp.feature.home.today.previewLogEntries

enum class HomeDestination(
    val label: String,
    val icon: ImageVector,
) {
    TODAY("Today", DwIcons.Drop),
    STATS("Stats", DwIcons.Wave),
    SETTINGS("Settings", DwIcons.Settings),
}

@PreviewScreenSizes
@Composable
internal fun HomeScreen() {
    var currentDestination by rememberSaveable { mutableStateOf(HomeDestination.TODAY) }

    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = DwMagenta,
            selectedTextColor = DwMagenta,
            indicatorColor = DwTintMagenta,
            unselectedIconColor = DwInk400,
            unselectedTextColor = DwInk400,
        )
    )

    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = DwInk100.copy(alpha = 0.96f),
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            HomeDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                    colors = itemColors,
                )
            }
        }
    ) {
        when (currentDestination) {
            HomeDestination.TODAY -> TodayScreen(
                name = "Alex",
                streak = 4,
                current = 1500,
                goal = 2400,
                log = previewLogEntries,
                onAdd = {},
                onCustom = {},
                onSeeAll = {},
            )

            HomeDestination.STATS -> StatsScreen()
            HomeDestination.SETTINGS -> SettingsScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DrinkWaterAppTheme {
        HomeScreen()
    }
}
