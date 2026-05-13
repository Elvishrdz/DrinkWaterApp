package com.eahm.drinkwaterapp.feature.dev.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.component.card.MiniCard
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTealSoft
import com.eahm.drinkwaterapp.core.presentation.composable.BuildInfoFooter
import com.eahm.drinkwaterapp.feature.dev.DevDestination
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DevMenuRoot(
    onMenuSelected: (DevDestination) -> Unit,
    viewModel: DevMenuViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DevMenuScreen(
        state = state,
        onMenuSelected = onMenuSelected,
    )
}

@Composable
private fun DevMenuScreen(
    state: DevMenuState,
    onMenuSelected: (DevDestination) -> Unit,
) {
    Scaffold { innerPadding ->
        Column {
            LazyVerticalGrid(
                modifier = Modifier
                    .weight(1f)
                    .padding(innerPadding),
                columns = GridCells.Adaptive(77.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 7.dp),
            ) {
                items(DevDestination.entries.filterNot { it == DevDestination.MENU }) {
                    MiniCard(
                        icon = it.icon,
                        iconBg = DwTealSoft,
                        iconTint = DwTealDark,
                        value = "",
                        label = it.label,
                        onClick = { onMenuSelected(it) },
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
            BuildInfoFooter(
                appVersion = state.appVersion,
                buildVariant = state.buildVariant,
            )
        }
    }
}

@Preview
@Composable
private fun DevMenuScreenPreview() {
    DevMenuScreen(
        state = DevMenuState(
            appVersion = "1.0.0",
            buildVariant = "Debug",
        ),
        onMenuSelected = {},
    )
}
