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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintMagenta
import com.eahm.drinkwaterapp.feature.dev.DevRoot
import com.eahm.drinkwaterapp.feature.drink.DrinkRoot
import com.eahm.drinkwaterapp.feature.history.v2.HistoryV2Root
import com.eahm.drinkwaterapp.feature.settings.SettingsScreen
import com.eahm.drinkwaterapp.feature.stats.StatsScreen
import com.eahm.drinkwaterapp.ui.confetti.ConfettiFadeMode
import com.eahm.drinkwaterapp.ui.confetti.ConfettiOverlay
import org.koin.compose.koinInject

enum class HomeDestination(
    val label: String,
    val icon: ImageVector,
) {
    TODAY("Today", DwIcons.Drop),
    STATS("Stats", DwIcons.Wave),
    HISTORY("History", DwIcons.History),
    SETTINGS("Settings", DwIcons.Settings),
    DEV("Development", DwIcons.Ruler),
}

@PreviewScreenSizes
@Composable
internal fun HomeScreen() {
    val confettiController = koinInject<ConfettiController>()
    val state by confettiController.state.collectAsStateWithLifecycle()
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
            HomeDestination.TODAY -> DrinkRoot(
                onNavigateToHistory = { currentDestination = HomeDestination.HISTORY },
                onNavigateToSettings = { currentDestination = HomeDestination.SETTINGS },
            )

            HomeDestination.STATS -> StatsScreen()
            HomeDestination.HISTORY -> HistoryV2Root()
            HomeDestination.SETTINGS -> SettingsScreen()
            HomeDestination.DEV -> DevRoot { currentDestination = HomeDestination.TODAY }
        }
    }

    ConfettiOverlay(
        active = state,
        fadeMode = ConfettiFadeMode.NONE,
        onFinished = confettiController::onFinished,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DrinkWaterAppTheme {
        HomeScreen()
    }
}
