package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.DrinkState
import com.eahm.drinkwaterapp.feature.drink.alarm.HomeAlarmCard

@Composable
internal fun InfoSection(
    state: DrinkState.Data,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    if (!isPreview) {
        HomeAlarmCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            onNavigateToSettings = onNavigateToSettings,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
    TodayLogSection(
        items = state.todayLogs,
        onSeeAll = onNavigateToHistory,
        modifier = Modifier.padding(horizontal = 20.dp),
        unitPreference = state.unitPreference,
    )
}

@Preview
@Composable
private fun InfoSectionPreview() {
    DrinkWaterAppTheme {
        InfoSection(
            state = DrinkState.Data(
                drinkStreakDays = 5,
                showStreakBadge = true,
                drinkStats = DrinkStats(
                    goalAmount = 2400f,
                    drinkAmount = 1500f,
                    goalReached = false,
                ),
                quickDrinkSuggestions = emptyList(),
                allDrinkSuggestions = emptyList(),
                todayLogs = previewLogEntries,
                alarmCountdownMs = null,
            ),
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview
@Composable
private fun InfoSectionEmptyPreview() {
    DrinkWaterAppTheme {
        InfoSection(
            state = DrinkState.Data(
                drinkStreakDays = 0,
                showStreakBadge = false,
                drinkStats = null,
                quickDrinkSuggestions = emptyList(),
                allDrinkSuggestions = emptyList(),
                todayLogs = emptyList(),
                alarmCountdownMs = null,
            ),
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}
