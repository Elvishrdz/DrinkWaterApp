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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.bottomsheet.AppBottomSheet
import kotlinx.coroutines.launch

private const val AMOUNT_STEP = 50
private const val AMOUNT_MIN = 50
private const val AMOUNT_MAX = 500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationAmountBottomSheet(
    currentAmountMl: Float,
    onAmountChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var value by remember {
        mutableIntStateOf(
            currentAmountMl.toInt().coerceIn(AMOUNT_MIN, AMOUNT_MAX)
                .let { it - (it % AMOUNT_STEP) }
                .coerceAtLeast(AMOUNT_MIN)
        )
    }

    val isAtMax = value >= AMOUNT_MAX
    val isAtMin = value <= AMOUNT_MIN

    fun dismissWithAnimation() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        NotificationAmountSheetContent(
            value = value,
            isAtMin = isAtMin,
            isAtMax = isAtMax,
            onDecrement = { value = (value - AMOUNT_STEP).coerceAtLeast(AMOUNT_MIN) },
            onIncrement = { value = (value + AMOUNT_STEP).coerceAtMost(AMOUNT_MAX) },
            onSave = {
                onAmountChanged(value.toFloat())
                dismissWithAnimation()
            },
        )
    }
}

@Composable
private fun NotificationAmountSheetContent(
    value: Int,
    isAtMin: Boolean,
    isAtMax: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onSave: () -> Unit,
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
            text = "Notification amount",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DwInk900,
        )

        Spacer(Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
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

            Spacer(Modifier.width(28.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$value",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp,
                    color = DwInk900,
                )
                Text(
                    text = "ml",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = DwInk400,
                )
            }

            Spacer(Modifier.width(28.dp))

            AmountStepperButton(
                enabled = !isAtMax,
                onClick = onIncrement,
            ) {
                Icon(
                    imageVector = DwIcons.Plus,
                    contentDescription = "Increase",
                    tint = if (!isAtMax) DwWater else DwWater.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Amount logged when tapping drink in the notification",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = DwInk500,
        )

        Spacer(Modifier.height(32.dp))

        DwPrimaryButton(
            text = "Save",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AmountStepperButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) DwTintWater else DwTintWater.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationAmountSheetContentDefaultPreview() {
    Surface {
        NotificationAmountSheetContent(
            value = 150,
            isAtMin = false,
            isAtMax = false,
            onDecrement = {},
            onIncrement = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationAmountSheetContentAtMinPreview() {
    Surface {
        NotificationAmountSheetContent(
            value = AMOUNT_MIN,
            isAtMin = true,
            isAtMax = false,
            onDecrement = {},
            onIncrement = {},
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationAmountSheetContentAtMaxPreview() {
    Surface {
        NotificationAmountSheetContent(
            value = AMOUNT_MAX,
            isAtMin = false,
            isAtMax = true,
            onDecrement = {},
            onIncrement = {},
            onSave = {},
        )
    }
}
