package com.eahm.drinkwaterapp.feature.drink.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.DrinkAction
import com.eahm.drinkwaterapp.feature.drink.DrinkState
import com.eahm.drinkwaterapp.feature.drink.alarm.HomeAlarmCard
import com.eahm.drinkwaterapp.feature.drink.drinkStateMock
import com.eahm.drinkwaterapp.feature.drink.ui.ActionsSection
import com.eahm.drinkwaterapp.feature.drink.ui.RingSection
import com.eahm.drinkwaterapp.feature.drink.ui.TodayLogSection

@Composable
internal fun DrinkLandscape(
    state: DrinkState.Data,
    onAction: (DrinkAction) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    leftContentWeight: Float = 1f,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(leftContentWeight)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))
            Spacer(Modifier.height(8.dp))
            RingSection(
                state = state,
                modifier = modifier
                    .fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(contentPadding.calculateTopPadding()))
            Spacer(Modifier.height(8.dp))

            ActionsSection(
                state = state,
                onAction = onAction,
            )

            Spacer(Modifier.height(16.dp))

            HomeAlarmCard(
                onNavigateToSettings = onNavigateToSettings,
            )

            Spacer(modifier = Modifier.height(16.dp))

            TodayLogSection(
                items = state.todayLogs,
                onSeeAll = onNavigateToHistory,
                unitPreference = state.unitPreference,
            )

            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
        }
    }
}

@Preview(name = "Landscape", widthDp = 900, heightDp = 420)
@Composable
private fun DrinkLandscapePreview() {
    DrinkWaterAppTheme {
        DrinkLandscape(
            state = drinkStateMock,
            onAction = {},
            onNavigateToHistory = {},
            onNavigateToSettings = {},
        )
    }
}
