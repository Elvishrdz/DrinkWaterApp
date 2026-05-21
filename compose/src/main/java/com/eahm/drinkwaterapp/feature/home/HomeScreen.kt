package com.eahm.drinkwaterapp.feature.home

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.ads.banner.InlineBannerAd
import com.eahm.drinkwaterapp.ads.banner.rememberBelowMainScreenBannerAdState
import com.eahm.drinkwaterapp.core.billing.PurchaseManager
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
    @StringRes val labelResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    TODAY(R.string.label_today, DwIcons.Drop, DwIcons.DropFilled),
    HISTORY(R.string.history_title, DwIcons.History, DwIcons.HistoryFilled),
    STATS(R.string.stats_title, DwIcons.BarChart, DwIcons.BarChartFilled),
    SETTINGS(R.string.settings_title, DwIcons.Settings, DwIcons.SettingsFilled),
    DEV(R.string.dev_title, DwIcons.Ruler, DwIcons.Ruler),
}

@PreviewScreenSizes
@Composable
internal fun HomeScreen() {
    val confettiController = koinInject<ConfettiController>()
    val purchaseManager = koinInject<PurchaseManager>()
    val state by confettiController.state.collectAsStateWithLifecycle()
    val isAdFree by purchaseManager.isAdFree.collectAsStateWithLifecycle()
    var currentDestination by rememberSaveable { mutableStateOf(HomeDestination.TODAY) }

    val bannerAdState = rememberBelowMainScreenBannerAdState(enabled = !isAdFree)

    // Show the DEV tab only in environments where dev tooling is intentional.
    // staging hides it (testers see the same UI as end users);
    // qa shows it (internal team validates dev tooling alongside features).
    val menus = if (BuildEnvironment.isDev || BuildEnvironment.isQa) {
        HomeDestination.entries
    } else {
        HomeDestination.entries.filterNot { it == HomeDestination.DEV }
    }

    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            // No pill indicator — the filled icon is the visual cue
            indicatorColor = Color.Transparent,
            unselectedIconColor = DwInk400,
            unselectedTextColor = DwInk400,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            // Keep a subtle indicator on the rail for larger screens
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = DwInk400,
            unselectedTextColor = DwInk400,
        ),
    )

    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.96f),
        navigationRailContainerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.96f),
    )

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            menus.forEach { destination ->
                val selected = destination == currentDestination
                item(
                    icon = {
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                            label = "scale_${destination.name}",
                        )
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.icon,
                            contentDescription = stringResource(destination.labelResId),
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        )
                    },
                    label = { Text(stringResource(destination.labelResId)) },
                    selected = selected,
                    onClick = { currentDestination = destination },
                    colors = itemColors,
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
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

            if (!isAdFree) {
                InlineBannerAd(
                    state = bannerAdState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
