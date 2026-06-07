package com.eahm.drinkwaterapp.feature.reminders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.feature.reminders.components.DaysOfWeekToggle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RemindersScreen(
    viewModel: RemindersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    RemindersContent(
        state = state,
        onDayToggled = viewModel::onDayToggled,
        onEnableAllDays = viewModel::onEnableAllDays,
    )
}

@Composable
private fun RemindersContent(
    state: RemindersState,
    onDayToggled: (Int) -> Unit,
    onEnableAllDays: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.reminders_title),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            item {
                ActiveDaysSection(
                    activeDays = state.activeDays,
                    onDayToggled = onDayToggled,
                    onEnableAllDays = onEnableAllDays,
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun ActiveDaysSection(
    activeDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    onEnableAllDays: () -> Unit,
) {
    val allEnabled = activeDays.size == 7

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reminders_section_active_days),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(
                visible = !allEnabled,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = onEnableAllDays,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = DwIcons.SevenDaysCheck,
                        contentDescription = stringResource(R.string.reminders_enable_all_days),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            DaysOfWeekToggle(
                activeDays = activeDays,
                onDayToggled = onDayToggled,
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun RemindersContentSomeDaysPreview() {
    DrinkWaterAppTheme {
        RemindersContent(
            state = RemindersState(activeDays = setOf(1, 2, 3, 4, 5)),
            onDayToggled = {},
            onEnableAllDays = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun RemindersContentAllDaysPreview() {
    DrinkWaterAppTheme {
        RemindersContent(
            state = RemindersState(activeDays = setOf(1, 2, 3, 4, 5, 6, 7)),
            onDayToggled = {},
            onEnableAllDays = {},
        )
    }
}

@Suppress("MagicNumber")
@PreviewScreenSizes
@Composable
private fun RemindersContentScreenSizesPreview() {
    DrinkWaterAppTheme {
        RemindersContent(
            state = RemindersState(activeDays = setOf(1, 2, 3, 4, 5, 6, 7)),
            onDayToggled = {},
            onEnableAllDays = {},
        )
    }
}
