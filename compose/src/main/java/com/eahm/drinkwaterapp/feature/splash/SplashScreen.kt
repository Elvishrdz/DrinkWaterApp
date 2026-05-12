package com.eahm.drinkwaterapp.feature.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to DwWaterLight,
                        0.45f to DwWater,
                        1.00f to DwWaterDeep,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SplashMark()

            Spacer(Modifier.height(16.dp))

            Text(
                text = "DRINK WATER",
                style = TextStyle(
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (22 * 0.16).sp,
                ),
                color = Color.White,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Stay hydrated, every day",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                ),
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun SplashMark() {
    val ringColor = Color.White.copy(alpha = 0.35f)
    val logoShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val strokePx = 1.dp.toPx()
            // r1 — outer ring
            drawCircle(
                color = ringColor,
                radius = cx,
                style = Stroke(strokePx),
            )
            // r2 — middle ring
            drawCircle(
                color = ringColor,
                radius = cx - 16.dp.toPx(),
                style = Stroke(strokePx),
            )
            // r3 — inner ring
            drawCircle(
                color = ringColor,
                radius = cx - 32.dp.toPx(),
                style = Stroke(strokePx),
            )
        }

        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "Drink Water logo",
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = logoShape,
                    ambientColor = DwWaterDeep,
                    spotColor = DwWaterDeep,
                )
                .clip(logoShape),
        )
    }
}

@DwPreview
@Composable
private fun SplashScreenPreview() {
    DrinkWaterAppTheme {
        SplashScreen()
    }
}
