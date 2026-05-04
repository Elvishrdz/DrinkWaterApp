package com.eahm.drinkwaterapp.feature.dev.alarms.countdown

import android.os.SystemClock
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import kotlinx.coroutines.delay

/**
 * Live countdown to an alarm.
 *
 * @param triggerAtElapsedMs The [SystemClock.elapsedRealtime] value at which the alarm fires
 *   (i.e. `AlarmStorageInfo.alarmTriggerAtMillis`).
 * @param config Controls which units to display and how zero values are handled.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlarmCountdown(
    triggerAtElapsedMs: Long,
    modifier: Modifier = Modifier,
    config: CountdownConfig = CountdownConfig(),
) {
    val remainingMs by produceState(
        initialValue = (triggerAtElapsedMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L),
        key1 = triggerAtElapsedMs,
    ) {
        while (true) {
            value = (triggerAtElapsedMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            if (value == 0L) break
            delay(1_000L)
        }
    }

    if (remainingMs == 0L) {
        CountdownExpired(modifier = modifier)
        return
    }

    val cells = remember(remainingMs, config) {
        buildCells(remainingMs, config)
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
    ) {
        cells.forEachIndexed { index, (unit, value) ->
            CountdownUnitCell(value = value.toInt(), label = unit.label)
            if (index < cells.lastIndex) {
                CountdownSeparator()
            }
        }
    }
}


private fun buildCells(
    remainingMs: Long,
    config: CountdownConfig,
): List<Pair<CountdownUnit, Long>> {
    // Decompose into each enabled unit in descending order
    val sorted = CountdownUnit.entries.filter { it in config.units }
    var rem = remainingMs.coerceAtLeast(0L)
    val decomposed = sorted.map { unit ->
        val value = rem / unit.inMs
        rem %= unit.inMs
        unit to value
    }

    if (!config.hideLeadingZeroUnits) return decomposed

    // Drop leading zero units, always keeping at least the last (smallest) unit
    val firstNonZero = decomposed.indexOfFirst { (_, v) -> v > 0L }
    return if (firstNonZero == -1) decomposed.takeLast(1)
    else decomposed.drop(firstNonZero)
}

@Composable
private fun CountdownUnitCell(value: Int, label: String) {
    val animated by animateIntAsState(
        targetValue = value,
        label = "countdown_$label",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
        Text(
            text = animated.toString().padStart(2, '0'),
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 52.sp,
            color = DwWaterDeep,
        )
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = DwInk500,
        )
    }
}

@Composable
private fun CountdownSeparator() {
    // Extra bottom padding aligns the colon with the number rows, not the unit labels
    Text(
        text = ":",
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        color = DwWaterDeep.copy(alpha = 0.35f),
        modifier = Modifier.padding(bottom = 20.dp),
    )
}

@Composable
private fun CountdownExpired(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "\u23F0", fontSize = 52.sp)   // ⏰
        Text(
            text = "Alarm triggered!",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DwMagenta,
        )
    }
}

fun Int.pad() = toString().padStart(2, '0')

@Preview(name = "Countdown active", showBackground = true)
@Composable
private fun AlarmCountdownPreview() {
    AlarmCountdown(
        triggerAtElapsedMs = SystemClock.elapsedRealtime() + 3_661_000L, // 1h 1m 1s
        config = CountdownConfig(
            units = setOf(CountdownUnit.HOURS, CountdownUnit.MINUTES, CountdownUnit.SECONDS),
        ),
    )
}

@Preview(name = "Countdown expired", showBackground = true)
@Composable
private fun AlarmCountdownExpiredPreview() {
    CountdownExpired()
}
