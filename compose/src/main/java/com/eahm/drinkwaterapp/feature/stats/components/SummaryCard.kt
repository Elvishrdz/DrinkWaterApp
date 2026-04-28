package com.eahm.drinkwaterapp.feature.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

private val gradientWaterHero = Brush.verticalGradient(
    colors = listOf(DwWaterLight, DwWaterDeep)
)

// Mint-green trend up color matching design system
private val TrendUpColor = Color(0xFFA3F7C6)
private val TrendDownColor = Color(0xFFFFCDD2)

@Composable
fun SummaryCard(
    avgMl: Int,
    trendPercent: Int,
    isTrendPositive: Boolean,
    periodLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(gradientWaterHero)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = "$periodLabel avg",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = Color.White
                    )
                ) {
                    append(avgMl.toString())
                }
                withStyle(
                    SpanStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                ) {
                    append("  mL/day")
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val arrow = if (isTrendPositive) "▲" else "▼"
            val trendColor = if (isTrendPositive) TrendUpColor else TrendDownColor
            val vsLabel = "vs previous period"
            Text(
                text = "$arrow $trendPercent%  $vsLabel",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = trendColor
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun SummaryCardPreview() {
    DrinkWaterAppTheme {
        SummaryCard(
            avgMl = 2150,
            trendPercent = 12,
            isTrendPositive = true,
            periodLabel = "This week's",
            modifier = Modifier.padding(16.dp)
        )
    }
}
