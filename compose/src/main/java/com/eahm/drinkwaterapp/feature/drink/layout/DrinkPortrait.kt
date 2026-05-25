package com.eahm.drinkwaterapp.feature.drink.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.drink.SlotPlaceholder

@Composable
internal fun PortraitLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    ringSection: @Composable () -> Unit,
    actionsSection: @Composable () -> Unit,
    infoSection: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(innerPadding.calculateTopPadding()))
        ringSection()
        Spacer(Modifier.height(20.dp))
        actionsSection()
        Spacer(Modifier.height(16.dp))
        infoSection()
        Spacer(Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
    }
}

@Preview(name = "Portrait")
@Composable
private fun PortraitLayoutPreview() {
    DrinkWaterAppTheme {
        PortraitLayout(
            innerPadding = PaddingValues(0.dp),
            ringSection = { SlotPlaceholder("Ring", Color(0xFF2196F3)) },
            actionsSection = { SlotPlaceholder("Actions", Color(0xFF4CAF50)) },
            infoSection = { SlotPlaceholder("Info", Color(0xFFFF9800)) },
        )
    }
}
