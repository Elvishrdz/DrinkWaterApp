package com.eahm.drinkwaterapp.feature.drink.ui

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.config.FeatureFlags
import com.eahm.drinkwaterapp.core.data.drinkstatsobserver.DrinkStats
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.preview.DwThemePreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.core.screenshot.ScreenshotModeController
import com.eahm.drinkwaterapp.feature.drink.DrinkState
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

private const val SCREENSHOT_EXIT_TAPS = 10

@Composable
internal fun RingSection(
    state: DrinkState.Data,
    modifier: Modifier = Modifier,
) {
    // Debug-only: 10 taps on the ring exits screenshot mode (the dev menu is hidden while it's on).
    val screenshotController = koinInject<ScreenshotModeController>()
    val screenshotMode by screenshotController.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var ringTapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (FeatureFlags.streakFeatureEnabled) {
            TodayHeader(
                streak = state.drinkStreakDays,
                showStreak = state.showStreakBadge,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            )
        }

        ProgressRing(
            current = state.drinkStats?.drinkAmount ?: 0f,
            goal = state.drinkStats?.goalAmount ?: 0f,
            displayZeroPercentage = false,
            onClick = {
                if (screenshotMode.enabled) {
                    ringTapCount++
                    if (ringTapCount >= SCREENSHOT_EXIT_TAPS) {
                        ringTapCount = 0
                        scope.launch { screenshotController.disable() }
                    }
                }
            },
            unitPreference = state.unitPreference,
        )

        Spacer(modifier = Modifier.height(10.dp))

        RotatingInfoPill(
            remaining = state.drinkStats?.remainingAmount ?: 0f,
            alarmCountdownMs = state.alarmCountdownMs,
            alarmIsExact = state.alarmIsExact,
            isOutsideActiveHours = state.isOutsideActiveHours,
        )
    }
}

@Composable
private fun TodayHeader(
    streak: Int,
    showStreak: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top,
    ) {
        StreakBadge(streak = streak, visible = showStreak)
    }
}

@Composable
private fun StreakBadge(
    streak: Int,
    visible: Boolean = true,
) {
    val gradientMagenta = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB83A95),
            DwMagentaDark
        )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(alpha = if (visible) 1f else 0f)
            .clip(RoundedCornerShape(999.dp))
            .background(gradientMagenta)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = DwIcons.Star,
            contentDescription = null,
            tint = DwPaper,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = stringResource(R.string.drink_streak_label, streak),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = DwPaper
        )
    }
}

private enum class PillMessage { Remaining, NextAlarm, OutsideActiveHours }

@Composable
private fun RotatingInfoPill(
    remaining: Float,
    alarmCountdownMs: Long?,
    alarmIsExact: Boolean,
    isOutsideActiveHours: Boolean,
    modifier: Modifier = Modifier,
) {
    val hasAlarm = alarmCountdownMs != null
    val messages = remember(hasAlarm, isOutsideActiveHours) {
        when {
            isOutsideActiveHours -> listOf(PillMessage.OutsideActiveHours, PillMessage.Remaining)
            hasAlarm -> listOf(PillMessage.NextAlarm, PillMessage.Remaining)
            else -> listOf(PillMessage.Remaining)
        }
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
            .background(MaterialTheme.colorScheme.secondaryContainer)
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

            val mlUnit = stringResource(R.string.volume_unit_mililiters)
            val remainingPrefix = stringResource(R.string.drink_remaining_prefix)
            val remainingSuffix = stringResource(R.string.drink_remaining_suffix)
            val goalReached = stringResource(R.string.drink_goal_reached)
            val goalReachedSuffix = stringResource(R.string.drink_goal_reached_suffix)
            val nextAlarmPrefix = stringResource(R.string.drink_next_alarm_prefix)
            val outsideActiveHoursText = stringResource(R.string.drink_outside_active_hours)

            when (message) {
                PillMessage.Remaining -> {
                    if (remaining > 0) {
                        Text(
                            text = buildAnnotatedString {
                                append(remainingPrefix)
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                ) {
                                    append("${remaining.toDigitString()} $mlUnit")
                                }
                                append(" ")
                                append(remainingSuffix)
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
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                ) {
                                    append(goalReached)
                                }
                                append(" ")
                                append(goalReachedSuffix)
                            },
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = DwInk400,
                            modifier = Modifier.scale(scale.value),
                        )
                    }
                }

                PillMessage.OutsideActiveHours -> {
                    Text(
                        text = outsideActiveHoursText,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = DwInk400,
                        modifier = Modifier.scale(scale.value),
                    )
                }

                PillMessage.NextAlarm -> {
                    val ms = alarmCountdownMs ?: 0L
                    if (ms == 0L) {
                        val firingText = stringResource(
                            if (alarmIsExact) R.string.drink_alarm_firing_now
                            else R.string.drink_alarm_firing_approx
                        )
                        Text(
                            text = firingText,
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = DwInk400,
                            modifier = Modifier.scale(scale.value),
                        )
                    } else {
                        Text(
                            text = buildAnnotatedString {
                                append(nextAlarmPrefix)
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = DwWater
                                    )
                                ) {
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

@DwPreview
@Composable
private fun RingSectionPreview() {
    DwThemePreview {
        Column {
            RingSection(
                state = DrinkState.Data(
                    isAdFree = false,
                    drinkStreakDays = 5,
                    showStreakBadge = true,
                    drinkStats = DrinkStats(
                        goalAmount = 2000f,
                        drinkAmount = 1200f,
                        goalReached = false,
                    ),
                    quickDrinkSuggestions = emptyList(),
                    allDrinkSuggestions = emptyList(),
                    todayLogs = emptyList(),
                    alarmCountdownMs = 3600000L, // 1 hour
                    alarmIsExact = true,
                )
            )
        }
    }
}
