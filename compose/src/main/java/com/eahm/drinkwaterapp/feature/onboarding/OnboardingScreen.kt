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
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepActivity
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepDone
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepExactAlarm
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepGender
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepInterval
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepName
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepNotifs
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepOverlay
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepSleep
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.StepWeight
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepIntro
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepSetGoal
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepUnits
import com.eahm.drinkwaterapp.feature.onboarding.ui.steps.stepWake
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
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
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri(),
            )
        )
    }

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

    // TODO: get from viewModel directly
    val step = OnboardingSteps.byStep(state.step)

    val stepConfig = when (step) {
        Intro -> stepIntro(
            onNext = { viewModel.nextStep() },
        )

        Units -> stepUnits(
            selectedUnit = state.unitPreference,
            onSelect = viewModel::onUnitPreferenceSelect,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        DailyGoal -> stepSetGoal(
            goalMl = state.goalMl,
            onGoalChanged = viewModel::onGoalChanged,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
            unitPreference = state.unitPreference,
        )

        RemindersSteps.StartTime -> stepWake(
            wakeTime = state.wakeTime,
            sleepTime = state.sleepTime,
            is24Hour = is24Hour,
            onWakeTimeChange = viewModel::onWakeTimeChange,
            onNext = { viewModel.nextStep() },
            onBack = { viewModel.prevStep() },
        )

        RemindersSteps.EndTime,
        RemindersSteps.Interval,
        RemindersSteps.Notification,
        RemindersSteps.ExactAlarm,
        Celebrations,
        Summary,
            -> stepIntro(
            onNext = { viewModel.nextStep() },
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
    }

    return

    CompositionLocalProvider(LocalOnboardingProgress provides animatedProgress) {

        //FIXME: adaptive layout flickering
        // create a single scafold and call it here to avoid flickering in the UI
        // after changing pages the ui shows an old layout and then changes to the current. fix this.
        when (OnboardingSteps.byStep(state.step)) {
            Intro -> TODO("")
            Units -> TODO("")
            DailyGoal -> TODO("")

            RemindersSteps.StartTime -> TODO()

            RemindersSteps.EndTime -> StepSleep(
                step = state.step,
                wakeTime = state.wakeTime,
                sleepTime = state.sleepTime,
                is24Hour = is24Hour,
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
                onNext = viewModel::nextStep,
                onBack = { viewModel.prevStep() },
            )

            // Permission check + amount config are handled inside StepNotifs.
            // onNext and onSkip both advance to ExactAlarm (step 7).
            RemindersSteps.Notification -> StepNotifs(
                step = state.step,
                intervalMinutes = state.intervalMinutes,
                notifAmountMl = state.notifAmountMl,
                onAmountChange = viewModel::onNotifAmountChange,
                onNext = { viewModel.nextStep() },
                onSkip = { viewModel.nextStep() },
                onBack = { viewModel.prevStep() },
                unitPreference = state.unitPreference,
            )

            RemindersSteps.ExactAlarm -> StepExactAlarm(
                step = state.step,
                exactAlarmGranted = canScheduleExactAlarms(),
                onAllow = { requestExactAlarmPermission() },
                onContinue = { viewModel.nextStep() },
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
