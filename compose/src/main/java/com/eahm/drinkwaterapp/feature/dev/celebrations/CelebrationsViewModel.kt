package com.eahm.drinkwaterapp.feature.dev.celebrations

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.eahm.drinkwaterapp.system.service.goalcelebration.GoalCelebrationOverlay
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationStyle
import com.eahm.drinkwaterapp.system.service.goalcelebration.extension.createIntent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class CelebrationsViewModel(
    val application: Application,
) : ViewModel() {

    private val _events = Channel<CelebrationsEvent>(Channel.BUFFERED)
    val events: Flow<CelebrationsEvent> = _events.receiveAsFlow()

    private val _state = MutableStateFlow<CelebrationsState>(CelebrationsState.Loading)
    val state: StateFlow<CelebrationsState> = _state

    init {
        _state.update { CelebrationsState.Data() }
    }

    fun onAction(action: CelebrationsAction) {
        when (action) {
            CelebrationsAction.ShowOceanStyle -> displayGoalReachedOverlay(CelebrationStyle.Ocean)
            CelebrationsAction.ShowCarnivalStyle -> displayGoalReachedOverlay(CelebrationStyle.Carnival)
        }
    }

    private fun displayGoalReachedOverlay(style: CelebrationStyle) {
        if (Settings.canDrawOverlays(application)) {
            val intent = GoalCelebrationOverlay.createIntent(
                context = application,
                style = style,
                goalMl = 2400f,
                drinkCount = 15,
                streakDays = 5,
            )

            application.startService(intent)
        }
    }
}
