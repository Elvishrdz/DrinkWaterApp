package com.eahm.drinkwaterapp.feature.dev.alarmcalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.data.drinkalarm.DayTime
import org.koin.compose.viewmodel.koinViewModel


@Composable
internal fun AlarmCalculatorRoot(
    onBackClicked: () -> Unit,
    viewModel: AlarmCalculatorViewModel = koinViewModel(),
) {
    val activeTimes by viewModel.activeTimes.collectAsStateWithLifecycle()
    val goalMl by viewModel.goalMl.collectAsStateWithLifecycle()
    val interval by viewModel.interval.collectAsStateWithLifecycle()

    activeTimes?.let {
        AlarmCalculatorScreen(
            startTime = DayTime(it.start.hour, it.start.minute),
            endTime = DayTime(it.end.hour, it.end.minute),
            initialGoalMl = goalMl,
            interval = interval,
            onBackClicked = onBackClicked,
        )
    }
}
