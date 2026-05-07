package com.eahm.drinkwaterapp.feature.drink

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.data.datastore.data.DrinkSuggestion
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
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
import com.eahm.drinkwaterapp.feature.drink.ui.ProgressRing
import com.eahm.drinkwaterapp.feature.drink.ui.QuickAddRow
import com.eahm.drinkwaterapp.feature.drink.ui.TodayLogSection
import com.eahm.drinkwaterapp.feature.drink.ui.previewLogEntries
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
internal fun DrinkScreen(
    state: DrinkState.Data,
    onDrink: (Float) -> Unit,
    onReorderPresets: (List<DrinkSuggestion>, Boolean) -> Unit,
    onCreatePreset: (Float) -> Unit,
    onDeletePreset: (DrinkSuggestion) -> Unit,
    onRestorePreset: (DrinkSuggestion) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = DwInk50,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Spacer(Modifier.height(it.calculateTopPadding()))

            TodayHeader(
                username = state.username,
                streak = state.currentStreak,
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
                    current = state.drinkStats?.drinkAmount ?: 0f,
                    goal = state.drinkStats?.goalAmount ?: 0f,
                )

                Spacer(modifier = Modifier.height(10.dp))

                RotatingInfoPill(
                    remaining = state.drinkStats?.remainingAmount ?: 0f,
                    alarmTriggerAtMs = state.alarmTriggerAtMs,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            QuickAddRow(
                quickPresets = state.quickDrinkSuggestions,
                allPresets = state.allDrinkSuggestions,
                onDrink = onDrink,
                onCreatePreset = onCreatePreset,
                onReorder = onReorderPresets,
                onDelete = onDeletePreset,
                onRestorePreset = onRestorePreset,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TodayLogSection(
                items = state.todayLogs,
                onSeeAll = onNavigateToHistory,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(it.calculateBottomPadding()))
        }
    }
}

@Composable
private fun TodayHeader(
    username: String?,
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
                text = "Good morning${if (username != null) "," else ""}",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DwInk400
            )

            username?.let {
                Text(
                    text = it,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    lineHeight = (26 * 1.2).sp,
                    color = DwInk900
                )
            }
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

private enum class PillMessage { Remaining, NextAlarm }

@Composable
private fun RotatingInfoPill(
    remaining: Float,
    alarmTriggerAtMs: Long?,
    modifier: Modifier = Modifier,
) {
    val alarmRemainingMs by produceState(
        initialValue = alarmTriggerAtMs?.let {
            (it - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        },
        key1 = alarmTriggerAtMs,
    ) {
        if (alarmTriggerAtMs == null) {
            value = null
            return@produceState
        }
        while (true) {
            val rem = (alarmTriggerAtMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            value = rem
            if (rem == 0L) break
            delay(1_000L)
        }
    }

    val hasAlarm = alarmRemainingMs != null && alarmRemainingMs!! > 0L
    val messages = remember(hasAlarm) {
        if (hasAlarm) listOf(PillMessage.NextAlarm, PillMessage.Remaining)
        else listOf(PillMessage.Remaining)
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(messages.size) {
        if (currentIndex >= messages.size) currentIndex = 0
        while (messages.size > 1) {
            val random = Random(10).nextLong(5, 15) * 1000L
            delay(random)
            currentIndex = (currentIndex + 1) % messages.size
        }
    }

    val currentMessage = messages[currentIndex.coerceAtMost(messages.lastIndex)]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DwTintWater)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = currentMessage,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "pill_message",
        ) { message ->
            val scale = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                scale.animateTo(1.08f, tween(80))
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }

            when (message) {
                PillMessage.Remaining -> {
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
                                    append("${remaining.toDigitString()} mL")
                                }
                                append(" away from today's goal")
                            },
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = DwInk400,
                            lineHeight = (13 * 1.45).sp,
                            modifier = Modifier.scale(scale.value),
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = DwWater
                                    )
                                ) {
                                    append("Goal reached")
                                }
                                append(" — keep sipping to stay ahead")
                            },
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = DwInk400,
                            modifier = Modifier.scale(scale.value),
                        )
                    }
                }

                PillMessage.NextAlarm -> {
                    val ms = alarmRemainingMs ?: 0L
                    Text(
                        text = buildAnnotatedString {
                            append("Next alarm in ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DwWater)) {
                                append(formatAlarmCountdown(ms))
                            }
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = DwInk400,
                        modifier = Modifier.scale(scale.value),
                    )
                }
            }
        }
    }
}

private fun formatAlarmCountdown(ms: Long): String {
    val hours = ms / (1000L * 60 * 60)
    val minutes = (ms / (1000L * 60)) % 60
    val seconds = (ms / 1000L) % 60
    return when {
        hours > 0L -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0L -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@Preview
@Composable
private fun DrinkScreenPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onNavigateToHistory = {}
        )
    }
}

@Preview
@Composable
private fun DrinkScreenEmptyPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onNavigateToHistory = {}
        )
    }
}

@Preview
@Composable
private fun DrinkScreenGoalReachedPreview() {
    DrinkWaterAppTheme {
        DrinkScreen(
            state = stateMock,
            onDrink = {},
            onReorderPresets = { _, _ -> },
            onCreatePreset = {},
            onDeletePreset = {},
            onRestorePreset = {},
            onNavigateToHistory = {}
        )
    }
}

internal val drinkSuggestionsMocks = listOf(
    DrinkSuggestion(1, 100f),
    DrinkSuggestion(2, 200f),
    DrinkSuggestion(3, 300f),
)

private val stateMock = DrinkState.Data(
    username = "Dave",
    currentStreak = 1,
    drinkStats = DrinkStats(
        goalAmount = 2400f,
        drinkAmount = 1500f,
        goalReached = false,
    ),
    quickDrinkSuggestions = drinkSuggestionsMocks.take(3),
    allDrinkSuggestions = drinkSuggestionsMocks,
    todayLogs = previewLogEntries.take(4),
    alarmTriggerAtMs = null,
)
