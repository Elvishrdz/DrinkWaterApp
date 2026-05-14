package com.eahm.drinkwaterapp.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.domain.extension.sanitizeZeroDecimals
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch

internal const val GOAL_STEP = 100f
internal const val GOAL_MIN = 100f
internal const val GOAL_MAX = 5_000f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalBottomSheet(
    goalMl: Float,
    onGoalChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var value by remember { mutableFloatStateOf(goalMl.coerceIn(GOAL_MIN, GOAL_MAX)) }

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Daily goal",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(32.dp))

            GoalStepperRow(
                value = value,
                onDecrement = { value -= GOAL_STEP },
                onIncrement = { value += GOAL_STEP },
            )

            Spacer(Modifier.height(32.dp))

            DwPrimaryButton(
                text = "Save",
                onClick = {
                    onGoalChanged(value)
                    dismissWithAnimation()
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun GoalStepperRow(
    value: Float,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            StepperCircleButton(
                enabled = value > GOAL_MIN,
                onClick = onDecrement,
                icon = { MinusLine() },
            )

            Spacer(Modifier.width(28.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value.sanitizeZeroDecimals(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "mL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = DwInk400,
                )
            }

            Spacer(Modifier.width(28.dp))

            StepperCircleButton(
                enabled = value < GOAL_MAX,
                onClick = onIncrement,
                icon = {
                    Icon(
                        imageVector = DwIcons.Plus,
                        contentDescription = "Increase",
                        tint = DwWater,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Min $GOAL_MIN mL · Max $GOAL_MAX mL",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = DwInk500,
        )
    }
}

@Composable
private fun StepperCircleButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        icon()
    }
}

/** A horizontal pill used as the minus indicator inside a stepper button. */
@Composable
private fun MinusLine() {
    Box(
        modifier = Modifier
            .size(width = 16.dp, height = 2.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DwWater),
    )
}
