package com.eahm.drinkwaterapp.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepActivity
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepDone
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepGender
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepGoal
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepInterval
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepIntro
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepName
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepNotifs
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepSleep
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepWake
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepWeight
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Notification permission launcher (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Whether granted or denied we proceed; the system handles the rest.
        viewModel.nextStep()
    }

    fun requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.nextStep()
        }
    }

    when (state.step) {
        1 -> StepIntro(
            onNext = { viewModel.nextStep() },
        )

        2 -> StepNotifs(
            onAllow = { requestNotifPermission() },
            onSkip = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        3 -> StepName(
            name = state.name,
            onNameChange = viewModel::onNameChange,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        4 -> StepGender(
            selected = state.gender,
            onSelect = viewModel::onGenderSelect,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        5 -> StepWeight(
            weightValue = state.weightValue,
            weightUnit = state.weightUnit,
            onWeightChange = viewModel::onWeightChange,
            onUnitChange = viewModel::onWeightUnitChange,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        6 -> StepActivity(
            selected = state.activityLevel,
            onSelect = viewModel::onActivitySelect,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        7 -> StepWake(
            wakeTime = state.wakeTime,
            onWakeTimeChange = viewModel::onWakeTimeChange,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        8 -> StepSleep(
            sleepTime = state.sleepTime,
            onSleepTimeChange = viewModel::onSleepTimeChange,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        9 -> StepInterval(
            selectedMinutes = state.intervalMinutes,
            onSelect = viewModel::onIntervalSelect,
            onNext = {
                viewModel.computeGoal()
                viewModel.nextStep()
            },
            onBack = { viewModel.prevStep() },
        )

        10 -> StepGoal(
            goalMl = state.computedGoalMl,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        else -> StepDone(
            state = state,
            onStart = { viewModel.finishOnboarding(onComplete) },
            onBack = { viewModel.prevStep() },
        )
    }
}

@DwPreview
@Composable
private fun OnboardingScreenPreview() {
    DrinkWaterAppTheme {
        StepIntro(onNext = {})
    }
}
