package com.eahm.drinkwaterapp.feature.history.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
internal fun SummaryCard(
    dayLabel: String,
    totalMl: Int,
    drinkCount: Int,
    percent: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DwPaper)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dayLabel,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk400,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = totalMl.toString(),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = DwInk900,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "mL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = DwInk400,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Text(
                text = "$drinkCount drink${if (drinkCount == 1) "" else "s"} · $percent% of goal",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = DwInk400,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(60.dp)) {
                val strokeW = 6.dp.toPx()
                val r = size.minDimension / 2f - strokeW / 2f
                val topLeft = Offset(center.x - r, center.y - r)
                val arcSize = Size(r * 2f, r * 2f)

                drawArc(
                    color = DwWaterLight.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize,
                )

                val sweep = (percent.coerceIn(0, 100) / 100f) * 360f
                if (sweep > 0f) {
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(DwWaterLight, DwWaterDeep),
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
            }
            Text(
                text = "$percent%",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = DwWaterDeep,
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun SummaryCardPreview() {
    DrinkWaterAppTheme {
        SummaryCard(
            dayLabel = "Today, May 10",
            totalMl = 1650,
            drinkCount = 6,
            percent = 75,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardEmptyPreview() {
    DrinkWaterAppTheme {
        SummaryCard(
            dayLabel = "Yesterday, May 9",
            totalMl = 0,
            drinkCount = 0,
            percent = 0,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardGoalMetPreview() {
    DrinkWaterAppTheme {
        SummaryCard(
            dayLabel = "Thu, May 8",
            totalMl = 2200,
            drinkCount = 9,
            percent = 100,
        )
    }
}
