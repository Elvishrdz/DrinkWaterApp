package com.eahm.drinkwaterapp.feature.onboarding

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.presentation.TimeFormatPreference
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Celebrations
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.DailyGoal
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Intro
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.RemindersSteps
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Summary
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingSteps.Units
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.LocalOnboardingProgress
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbShell
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.OnboardingStep
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepDone
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepExactAlarm
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepInterval
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepIntro
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepNotifs
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepOverlay
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepSetGoal
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepSleep
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepUnits
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepWake
import com.eahm.drinkwaterapp.feature.settings.ui.IntervalBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Exact alarm permission launcher (Android 12+).
    // Auto-advances when the user grants the permission and returns to the app.
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == true) {
                viewModel.nextStep()
            }
        }
    }

    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            exactAlarmLauncher.launch(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            )
        }
    }

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() == true
        } else {
            true
        }

    val is24Hour = when (state.timeFormatPreference) {
        TimeFormatPreference.SYSTEM -> DateFormat.is24HourFormat(context)
        TimeFormatPreference.HOUR_12 -> false
        TimeFormatPreference.HOUR_24 -> true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = state.step.toFloat() / OnboardingSteps.totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "onboarding_progress",
    )

    BackHandler(enabled = state.step > 1) {
        viewModel.prevStep()
    }

    val step = OnboardingSteps.byStep(state.step)

    var showCustomIntervalBottomSheet by rememberSaveable { mutableStateOf(false) }
    var currentMinutes by rememberSaveable { mutableIntStateOf(0) }
    var windowMinutes by rememberSaveable { mutableIntStateOf(0) }

    val stepConfig = when (step) {
        Intro -> stepIntro(
            onNext = viewModel::nextStep,
        )

        Units -> stepUnits(
            selectedUnit = state.unitPreference,
            onSelect = viewModel::onUnitPreferenceSelect,
            onNext = viewModel::nextStep,
            onBack = viewModel::prevStep,
        )

        DailyGoal -> stepSetGoal(
            goalMl = state.goalMl,
            onGoalChanged = viewModel::onGoalChanged,
            onNext = viewModel::nextStep,
            onBack = viewModel::prevStep,
            unitPreference = state.unitPreference,
        )

        RemindersSteps.StartTime -> stepWake(
            wakeTime = state.wakeTime,
            sleepTime = state.sleepTime,
            is24Hour = is24Hour,
            onWakeTimeChange = viewModel::onWakeTimeChange,
            onNext = viewModel::nextStep,
            onBack = viewModel::prevStep,
        )

        RemindersSteps.EndTime -> stepSleep(
            wakeTime = state.wakeTime,
            sleepTime = state.sleepTime,
            is24Hour = is24Hour,
            onSleepTimeChange = viewModel::onSleepTimeChange,
            onNext = viewModel::nextStep,
            onBack = viewModel::prevStep,
        )

        RemindersSteps.Interval -> stepInterval(
            selectedMinutes = state.intervalMinutes,
            wakeTime = state.wakeTime,
            sleepTime = state.sleepTime,
            onSelect = viewModel::onIntervalSelect,
            onNext = viewModel::nextStep,
            onBack = viewModel::prevStep,
            onCustomInterval = { minutes, windowMin ->
                currentMinutes = minutes
                windowMinutes = windowMin
                showCustomIntervalBottomSheet = true
            },
        )

        RemindersSteps.Notification -> stepNotifs(
            intervalMinutes = state.intervalMinutes,
            notifAmountMl = state.notifAmountMl,
            onAmountChange = viewModel::onNotifAmountChange,
            onNext = viewModel::nextStep,
            onSkip = viewModel::nextStep,
            onBack = viewModel::prevStep,
            unitPreference = state.unitPreference,
        )

        RemindersSteps.ExactAlarm -> stepExactAlarm(
            exactAlarmGranted = canScheduleExactAlarms(),
            onAllow = ::requestExactAlarmPermission,
            onContinue = viewModel::nextStep,
            onSkip = viewModel::nextStep,
            onBack = viewModel::prevStep,
        )

        Celebrations -> stepOverlay(
            onContinue = viewModel::nextStep,
            onBack = viewModel::prevStep,
        )

        Summary -> stepDone(
            state = state,
            is24Hour = is24Hour,
            onStart = { viewModel.finishOnboarding(onComplete) },
            onBack = viewModel::prevStep,
        )
    }

    CompositionLocalProvider(LocalOnboardingProgress provides animatedProgress) {
        OnbShell(
            step = state.step,
            total = OnboardingSteps.totalSteps,
            title = stepConfig.title,
            subtitle = stepConfig.subtitle,
            art = stepConfig.art,
            onBack = stepConfig.onBack,
            footer = stepConfig.footer,
            content = stepConfig.content,
        )

        if (showCustomIntervalBottomSheet) {
            IntervalBottomSheet(
                currentMinutes = currentMinutes,
                windowMinutes = windowMinutes,
                onIntervalChanged = viewModel::onIntervalSelect,
                onDismiss = { showCustomIntervalBottomSheet = false },
            )
        }
    }
}

@Composable
internal fun OnboardingStepBasePreview(config: OnboardingStep) {
    OnbShell(
        step = 1,
        total = OnboardingSteps.totalSteps,
        title = config.title,
        subtitle = config.subtitle,
        art = config.art,
        onBack = config.onBack,
        footer = config.footer,
        content = config.content,
    )
}
