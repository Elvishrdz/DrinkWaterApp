package com.eahm.drinkwaterapp.feature.dev.wateranimation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eahm.drinkwaterapp.core.data.datastore.PrefsStorage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

internal data class WaterAnimationDevState(val animationEnabled: Boolean)

internal class WaterAnimationDevViewModel(
    private val prefsStorage: PrefsStorage,
) : ViewModel() {

    val state = prefsStorage.getWaterAnimationEnabled()
        .map { WaterAnimationDevState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = WaterAnimationDevState(true),
        )

    fun onToggle(enabled: Boolean) {
        viewModelScope.launch { prefsStorage.setWaterAnimationEnabled(enabled) }
    }
}

@Composable
internal fun WaterAnimationDevRoot(
    onBackClicked: () -> Unit,
    viewModel: WaterAnimationDevViewModel = koinViewModel(),
) {
    BackHandler(onBack = onBackClicked)
    val state by viewModel.state.collectAsStateWithLifecycle()
    WaterAnimationDevScreen(
        state = state,
        onToggle = viewModel::onToggle,
        onBackClicked = onBackClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterAnimationDevScreen(
    state: WaterAnimationDevState,
    onToggle: (Boolean) -> Unit,
    onBackClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Animation") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) { Text("← Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Full-screen rising water animation", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Triggered when logging a drink via a quick preset or preset bottom sheet. " +
                        "Custom amount entries do not trigger the animation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Animation enabled",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = if (state.animationEnabled) "Active" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.animationEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.animationEnabled,
                        onCheckedChange = onToggle,
                    )
                }
            }
        }
    }
}
