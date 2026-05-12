package com.eahm.drinkwaterapp.feature.onboarding

import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Celebrations
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.DailyGoal
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Intro
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.RemindersSteps
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Summary
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.LocalOnboardingProgress
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepActivity
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepDone
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepGender
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepInterval
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepIntro
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepName
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepNotifs
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepOverlay
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepSetGoal
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
    val context = LocalContext.current

    // Overlay ("display over other apps") permission launcher.
    // Auto-advances when the user grants permission and returns to the app.
    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (Settings.canDrawOverlays(context)) {
            viewModel.nextStep()
        }
    }

    fun requestOverlayPermission() {
        overlayLauncher.launch(
            android.content.Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri(),
            )
        )
    }

    val animatedProgress by animateFloatAsState(
        targetValue = state.step.toFloat() / OnboardingSteps.totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "onboarding_progress",
    )

    CompositionLocalProvider(LocalOnboardingProgress provides animatedProgress) {
        when (OnboardingSteps.byStep(state.step)) {
            Intro -> StepIntro(
                step = state.step,
                onNext = { viewModel.nextStep() },
            )

            DailyGoal -> StepSetGoal(
                step = state.step,
                goalMl = state.computedGoalMl,
                onGoalChanged = viewModel::onComputedGoalChange,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
            )

            RemindersSteps.StartTime -> StepWake(
                step = state.step,
                wakeTime = state.wakeTime,
                sleepTime = state.sleepTime,
                onWakeTimeChange = viewModel::onWakeTimeChange,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
            )

            RemindersSteps.EndTime -> StepSleep(
                step = state.step,
                wakeTime = state.wakeTime,
                sleepTime = state.sleepTime,
                onSleepTimeChange = viewModel::onSleepTimeChange,
                onNext = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
            )

            RemindersSteps.Interval -> StepInterval(
                step = state.step,
                selectedMinutes = state.intervalMinutes,
                wakeTime = state.wakeTime,
                sleepTime = state.sleepTime,
                onSelect = viewModel::onIntervalSelect,
                onNext = {
                    viewModel.computeGoal()
                    viewModel.nextStep()
                },
                onBack = { viewModel.prevStep() },
            )

            // Permission check + amount config are handled inside StepNotifs.
            // onNext and onSkip both advance to Summary (step 7).
            RemindersSteps.Notification -> StepNotifs(
                step = state.step,
                intervalMinutes = state.intervalMinutes,
                notifAmountMl = state.notifAmountMl,
                onAmountChange = viewModel::onNotifAmountChange,
                onNext = { viewModel.nextStep() },
                onSkip = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
            )

            Celebrations -> StepOverlay(
                step = state.step,
                overlayGranted = Settings.canDrawOverlays(context),
                onAllow = { requestOverlayPermission() },
                onContinue = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
            )

            Summary -> StepDone(
                step = state.step,
                state = state,
                onStart = { viewModel.finishOnboarding(onComplete) },
                onBack = { viewModel.prevStep() },
            )
        }
    }

    // Legacy
    if (false) {
        when (state.step) {
            // steps for new features in the future
            3 -> StepName(
                step = state.step,
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
        }
    }
}

@DwPreview
@Composable
private fun OnboardingScreenPreview() {
    DrinkWaterAppTheme {
        StepIntro(
            step = 1,
            onNext = {},
        )
    }
}
