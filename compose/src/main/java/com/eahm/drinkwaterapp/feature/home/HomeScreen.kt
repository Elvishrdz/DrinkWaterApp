package com.eahm.drinkwaterapp.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.ads.banner.DrinkWaterBannerAd
import com.eahm.drinkwaterapp.core.config.BuildEnvironment
import com.eahm.drinkwaterapp.core.data.confetti.ConfettiController
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.feature.dev.DevRoot
import com.eahm.drinkwaterapp.feature.drink.DrinkRoot
import com.eahm.drinkwaterapp.feature.history.HistoryRoot
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
    HISTORY("History", DwIcons.History),
    STATS("Stats", DwIcons.Wave),
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
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = DwInk400,
            unselectedTextColor = DwInk400,
        )
    )

    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.96f),
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            val menus = if (BuildEnvironment.isProduction) {
                HomeDestination.entries.filterNot { it == HomeDestination.DEV }
            } else {
                HomeDestination.entries
            }

            menus.forEach { destination ->
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
        Column(Modifier.fillMaxSize()) {
            Column(Modifier
                .fillMaxSize()
                .weight(1f)) {
                when (currentDestination) {
                    HomeDestination.TODAY -> DrinkRoot(
                        onNavigateToHistory = { currentDestination = HomeDestination.HISTORY },
                        onNavigateToSettings = { currentDestination = HomeDestination.SETTINGS },
                    )

                    HomeDestination.HISTORY -> HistoryRoot()
                    HomeDestination.STATS -> StatsScreen()
                    HomeDestination.SETTINGS -> SettingsScreen()
                    HomeDestination.DEV -> DevRoot { currentDestination = HomeDestination.TODAY }
                }
            }

            //TODO: set banner together with the bottom navigation instead
            DrinkWaterBannerAd(
                modifier = Modifier.fillMaxWidth()
            )
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
