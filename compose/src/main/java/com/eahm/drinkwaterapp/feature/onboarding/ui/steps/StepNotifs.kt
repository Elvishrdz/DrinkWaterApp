package com.eahm.drinkwaterapp.feature.onboarding.ui.steps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwSecondaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.canShowNotifications
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import com.eahm.drinkwaterapp.feature.onboarding.OnboardingStepBasePreview
import com.eahm.drinkwaterapp.feature.onboarding.ui.components.DwPrimaryButtonAnimated
import com.eahm.drinkwaterapp.feature.onboarding.ui.shell.OnbArtCircle
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_MAX
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_MIN
import com.eahm.drinkwaterapp.feature.settings.ui.AMOUNT_STEP
import com.eahm.drinkwaterapp.feature.settings.ui.DisplayUnit
import com.eahm.drinkwaterapp.feature.settings.ui.effectiveDisplayUnit
import kotlin.math.roundToInt

@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun stepNotifs(
    intervalMinutes: Int,
    notifAmountMl: Float,
    onAmountChange: (Float) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    unitPreference: UnitPreference = UnitPreference.ML,
): OnboardingStep {
    val context = LocalContext.current
    val hasNotificationPermission = context.canShowNotifications


    var showQuickDrinkStep by rememberSaveable(hasNotificationPermission) {
        mutableStateOf(hasNotificationPermission)
    }

    return if (showQuickDrinkStep) {
        var localAmount by rememberSaveable(notifAmountMl) {
            mutableIntStateOf(
                notifAmountMl.toInt()
                    .coerceIn(AMOUNT_MIN, AMOUNT_MAX)
                    .let { it - (it % AMOUNT_STEP) }
                    .coerceAtLeast(AMOUNT_MIN)
            )
        }

        stepQuickDrink(
            unitPreference = unitPreference,
            localAmount = localAmount,
            onBack = onBack,
            onContinue = {
                onAmountChange(localAmount.toFloat())
                onNext()
            },
            onDecrement = {
                localAmount = (localAmount - AMOUNT_STEP).coerceAtLeast(AMOUNT_MIN)
            },
            onIncrement = {
                localAmount = (localAmount + AMOUNT_STEP).coerceAtMost(AMOUNT_MAX)
            },
            onValueSet = { localAmount = it }
        )

    } else {
        var showPermissionDeniedDialog by rememberSaveable { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) showQuickDrinkStep = true
            else showPermissionDeniedDialog = true
        }

        fun requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showQuickDrinkStep = true
            }
        }

        if (showPermissionDeniedDialog) {
            val onDismiss = { showPermissionDeniedDialog = false }

            BasicAlertDialog(onDismissRequest = onDismiss) {
                PermissionDeniedContent(
                    onGoToSettings = {
                        showPermissionDeniedDialog = false
                        val intent = Intent().apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            } else {
                                @Suppress("DEPRECATION")
                                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                putExtra("app_package", context.packageName)
                                putExtra("app_uid", context.applicationInfo.uid)
                            }
                        }
                        context.startActivity(intent)
                    },
                    onDismiss = onDismiss,
                )
            }
        }

        stepRequestNotificationPermission(
            intervalMinutes = intervalMinutes,
            onBack = onBack,
            onSkip = onSkip,
            onRequestNotificationPermission = ::requestPermission,
        )
    }
}


@Composable
private fun stepQuickDrink(
    unitPreference: UnitPreference,
    localAmount: Int,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onValueSet: (Int) -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Glass) },
    title = stringResource(R.string.onb_notifs_config_title),
    subtitle = stringResource(R.string.onb_notifs_config_subtitle),
    onBack = onBack,
    footer = {
        DwPrimaryButtonAnimated(
            text = stringResource(R.string.onb_cta_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AmountStepper(
                value = localAmount,
                isAtMin = localAmount <= AMOUNT_MIN,
                isAtMax = localAmount >= AMOUNT_MAX,
                onDecrement = onDecrement,
                onIncrement = onIncrement,
                onValueSet = onValueSet,
                unitPreference = unitPreference,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onb_notifs_config_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    },
)

@Composable
private fun stepRequestNotificationPermission(
    intervalMinutes: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
) = OnboardingStep(
    art = { OnbArtCircle(icon = DwIcons.Bell) },
    title = stringResource(R.string.onb_notifs_permission_title),
    subtitle = stringResource(
        R.string.onb_notifs_permission_subtitle,
        formatIntervalLabel(intervalMinutes)
    ),
    onBack = onBack,
    footer = {
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.onb_notifs_permission_cta_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        DwPrimaryButton(
            text = stringResource(R.string.onb_notifs_permission_cta_allow),
            onClick = onRequestNotificationPermission,
            modifier = Modifier.fillMaxWidth(),
        )
    },
    content = {
        PermissionCard()
    },
)

@Composable
private fun PermissionCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "App icon",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(0.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.onb_notifs_permission_card_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onb_notifs_permission_card_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
private fun AmountStepper(
    unitPreference: UnitPreference,
    value: Int,
    isAtMin: Boolean,
    isAtMax: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onValueSet: (Int) -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val displayUnit = effectiveDisplayUnit(unitPreference, value.toFloat())

    val (minDisplay, maxDisplay) = when (displayUnit) {
        DisplayUnit.ML -> Pair(AMOUNT_MIN.toFloat(), AMOUNT_MAX.toFloat())
        DisplayUnit.FL_OZ -> Pair(AMOUNT_MIN / 29.574f, AMOUNT_MAX / 29.574f)
        DisplayUnit.L -> Pair(AMOUNT_MIN / 1000f, AMOUNT_MAX / 1000f)
    }

    fun formatBound(v: Float) =
        if (displayUnit == DisplayUnit.ML) v.toInt().toString() else "%.1f".format(v)

    @SuppressLint("LocalContextGetResourceValueCall")
    fun trySave() {
        val parsed = textInput.trim().toFloatOrNull()
        when {
            parsed == null ->
                errorMessage = context.getString(R.string.validation_error_invalid_number)

            parsed < minDisplay ->
                errorMessage =
                    context.getString(R.string.validation_error_min_value, formatBound(minDisplay))

            parsed > maxDisplay ->
                errorMessage =
                    context.getString(R.string.validation_error_max_value, formatBound(maxDisplay))

            else -> {
                val normalisedDisplay = when (displayUnit) {
                    DisplayUnit.ML -> parsed.roundToInt().toFloat()
                    DisplayUnit.FL_OZ -> (parsed * 10).roundToInt() / 10f
                    DisplayUnit.L -> (parsed * 10).roundToInt() / 10f
                }
                val amountMl = when (displayUnit) {
                    DisplayUnit.ML -> normalisedDisplay
                    DisplayUnit.FL_OZ -> normalisedDisplay * 29.574f
                    DisplayUnit.L -> normalisedDisplay * 1000f
                }
                onValueSet(amountMl.roundToInt())
                isEditing = false
                textInput = ""
                errorMessage = ""
                focusManager.clearFocus()
            }
        }
    }

    AnimatedContent(
        targetState = isEditing,
        label = "notif_amount_stepper_edit",
    ) { editing ->
        if (!editing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AmountStepperButton(
                    enabled = !isAtMin,
                    onClick = onDecrement,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (!isAtMin) DwWater else DwWater.copy(alpha = 0.4f)),
                    )
                }

                Column(
                    modifier = Modifier
                        .clickable { isEditing = true },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value.toFloat().formatVolume(unitPreference),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 56.sp,
                        lineHeight = 56.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                AmountStepperButton(
                    enabled = !isAtMax,
                    onClick = onIncrement,
                ) {
                    Icon(
                        imageVector = DwIcons.Plus,
                        contentDescription = "Increase",
                        tint = if (!isAtMax) DwWater else DwWater.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it; errorMessage = "" },
                    label = { Text("${stringResource(R.string.settings_stepper_custom_hint)} (${displayUnit.label})") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { trySave() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = {
                        isEditing = false
                        textInput = ""
                        errorMessage = ""
                        focusManager.clearFocus()
                    }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(onClick = { trySave() }) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountStepperButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        content()
    }
}

@Composable
private fun PermissionDeniedContent(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.notif_disabled_title),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(
                R.string.notif_disabled_body,
                stringResource(R.string.app_name)
            ),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = DwInk500,
        )
        Spacer(Modifier.height(24.dp))
        DwPrimaryButton(
            text = stringResource(R.string.action_open_settings),
            onClick = onGoToSettings,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        DwSecondaryButton(
            text = stringResource(R.string.action_cancel),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** "45m", "1h", "1h 30m" */
internal fun formatIntervalLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0 || h == 0) append("${m}m")
    }.trim()
}

@Composable
private fun StepNotifsPermissionBasePreview() {
    OnboardingStepBasePreview(
        config = stepNotifs(
            intervalMinutes = 45,
            notifAmountMl = 150f,
            onAmountChange = {},
            onNext = {},
            onSkip = {},
            onBack = {},
        ),
    )
}

@Composable
private fun StepRequestNotificationPermissionBasePreview() {
    OnboardingStepBasePreview(
        config = stepRequestNotificationPermission(
            intervalMinutes = 60,
            onBack = {},
            onSkip = {},
            onRequestNotificationPermission = {},
        ),
    )
}

@Preview
@Composable
private fun StepRequestNotificationPermissionPreview() {
    DrinkWaterAppTheme {
        StepRequestNotificationPermissionBasePreview()
    }
}


@Composable
private fun StepQuickDrinkBasePreview() {
    OnboardingStepBasePreview(
        config = stepQuickDrink(
            unitPreference = UnitPreference.ML,
            localAmount = 100,
            onBack = {},
            onContinue = {},
            onDecrement = {},
            onIncrement = {},
            onValueSet = {},
        ),
    )
}

@Preview
@Composable
private fun StepQuickDrinkPreview() {
    DrinkWaterAppTheme {
        StepQuickDrinkBasePreview()
    }
}

@Composable
private fun StepNotifsAmountBasePreview() {
    OnboardingStepBasePreview(
        config = OnboardingStep(
            art = { OnbArtCircle(icon = DwIcons.Glass) },
            title = stringResource(R.string.onb_notifs_config_title),
            subtitle = stringResource(R.string.onb_notifs_config_subtitle),
            onBack = {},
            footer = {
                DwPrimaryButtonAnimated(
                    text = stringResource(R.string.onb_cta_continue),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AmountStepper(
                        unitPreference = UnitPreference.ML,
                        value = 150,
                        isAtMin = false,
                        isAtMax = false,
                        onDecrement = {},
                        onIncrement = {},
                        onValueSet = {},
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.onb_notifs_config_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            },
        ),
    )
}

@DwPreview
@Composable
private fun StepNotifsPermissionPreview() {
    DrinkWaterAppTheme {
        StepNotifsPermissionBasePreview()
    }
}

@DwPreview
@Composable
private fun StepNotifsAmountPreview() {
    DrinkWaterAppTheme {
        StepNotifsAmountBasePreview()
    }
}

@Preview
@Composable
private fun PermissionCardPreview() {
    PermissionCard()
}

@Preview
@Composable
private fun AmountStepperPreview() {
    AmountStepper(
        unitPreference = UnitPreference.ML,
        value = 100,
        isAtMin = false,
        isAtMax = false,
        onDecrement = {},
        onIncrement = {},
        onValueSet = {},
    )
}

@Preview
@Composable
private fun PermissionDeniedContentPreview() {
    PermissionDeniedContent(
        onGoToSettings = {},
        onDismiss = {},
    )
}
