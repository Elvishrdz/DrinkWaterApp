package com.eahm.drinkwaterapp.feature.home.today

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk50
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk900
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwTintWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans

@Composable
fun TodayScreen(
    name: String,
    streak: Int,
    current: Int,
    goal: Int,
    log: List<DrinkLogEntry>,
    onAdd: (Int) -> Unit,
    onCustom: () -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val percent = (current.toFloat() / goal.toFloat() * 100f).coerceIn(0f, 100f)
    val remaining = (goal - current).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DwInk50)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        TodayHeader(
            name = name,
            streak = streak,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            ProgressRing(
                percent = percent,
                current = current,
                goal = goal
            )

            Spacer(modifier = Modifier.height(10.dp))

            RemainingPill(remaining = remaining, current = current, goal = goal)
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuickAddRow(
            onAdd = onAdd,
            onCustom = onCustom,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TodayLogSection(
            items = log,
            onSeeAll = onSeeAll,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TodayHeader(
    name: String,
    streak: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Good morning,",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk400
            )
            Text(
                text = name,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = (26 * 1.2).sp,
                color = DwInk900
            )
        }

        StreakBadge(streak = streak)
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    val gradientMagenta = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB83A95),
            DwMagentaDark
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(gradientMagenta)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = DwIcons.Star,
            contentDescription = null,
            tint = DwPaper,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$streak-day streak",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = DwPaper
        )
    }
}

@Composable
private fun RemainingPill(
    remaining: Int,
    current: Int,
    goal: Int,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DwTintWater)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (remaining > 0) {
            Text(
                text = buildAnnotatedString {
                    append("You're ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = DwWater
                        )
                    ) {
                        append("$remaining mL")
                    }
                    append(" away from today's goal")
                },
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                lineHeight = (13 * 1.45).sp
            )
        } else {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DwWater)) {
                        append("Goal reached")
                    }
                    append(" — keep sipping to stay ahead")
                },
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400
            )
        }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    DrinkWaterAppTheme {
        TodayScreen(
            name = "Alex",
            streak = 4,
            current = 1500,
            goal = 2400,
            log = previewLogEntries,
            onAdd = {},
            onCustom = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenEmptyPreview() {
    DrinkWaterAppTheme {
        TodayScreen(
            name = "Alex",
            streak = 0,
            current = 0,
            goal = 2400,
            log = emptyList(),
            onAdd = {},
            onCustom = {},
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenGoalReachedPreview() {
    DrinkWaterAppTheme {
        TodayScreen(
            name = "Alex",
            streak = 12,
            current = 2500,
            goal = 2400,
            log = previewLogEntries,
            onAdd = {},
            onCustom = {},
            onSeeAll = {}
        )
    }
}
