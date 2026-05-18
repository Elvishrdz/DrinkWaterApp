package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.domain.extension.formatVolume
import com.eahm.drinkwaterapp.core.presentation.UnitPreference
import kotlinx.coroutines.launch

@Composable
fun WaterChip(
    amount: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateOnClick: Boolean = false,
    fillColor: Color = DwWater,
    fillDurationMs: Int = 450,
    fadeDurationMs: Int = 300,
    unitPreference: UnitPreference = UnitPreference.ML,
) {
    val animatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .outlineBorderStyle()
            .clickable {
                onClick()
                if (animateOnClick) {
                    scope.launch {
                        animatable.snapTo(0f)
                        animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(fillDurationMs, easing = FastOutSlowInEasing),
                        )
                        animatable.animateTo(
                            targetValue = 2f,
                            animationSpec = tween(fadeDurationMs, easing = LinearEasing),
                        )
                        animatable.snapTo(0f)
                    }
                }
            }
            .drawBehind {
                val v = animatable.value
                if (v > 0f) {
                    val fillProgress = v.coerceIn(0f, 1f)
                    val fadeAlpha = 1f - (v - 1f).coerceIn(0f, 1f)
                    val fillHeight = size.height * fillProgress
                    drawRect(
                        color = fillColor.copy(alpha = 0.28f * fadeAlpha),
                        topLeft = Offset(0f, size.height - fillHeight),
                        size = Size(size.width, fillHeight),
                    )
                }
            }
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Icon(
            imageVector = DwIcons.Glass,
            contentDescription = null,
            tint = DwWater,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount.formatVolume(unitPreference),
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = DwInk900,
        )
    }
}

@Preview
@Composable
private fun WaterChipPreview() {
    DrinkWaterAppTheme {
        WaterChip(
            amount = 10f,
            onClick = {},
        )
    }
}
