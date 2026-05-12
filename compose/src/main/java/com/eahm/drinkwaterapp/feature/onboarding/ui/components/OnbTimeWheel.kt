package com.eahm.drinkwaterapp.feature.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.preview.DwPreview
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterDeep
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWaterLight
import com.eahm.drinkwaterapp.feature.onboarding.OnbTimeSelection

private val ChevronUp: ImageVector by lazy {
    ImageVector.Builder(
        name = "OnbChevronUp",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { moveTo(6f, 15f); lineTo(12f, 9f); lineTo(18f, 15f) }
    }.build()
}

private val ChevronDown: ImageVector by lazy {
    ImageVector.Builder(
        name = "OnbChevronDown",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) }
    }.build()
}

/**
 * A tactile time-picker wheel showing hour : minute with an AM / PM toggle.
 *
 * Hours cycle 1–12; minutes step in 5-minute increments.
 * Matches the design-system TimeWheel component from Onboarding.jsx.
 */
@Composable
internal fun OnbTimeWheel(
    time: OnbTimeSelection,
    onTimeChange: (OnbTimeSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Hour spinner
        TimeSpinnerCell(
            value = time.hour.toString().padStart(2, '0'),
            onIncrement = {
                val next = if (time.hour >= 12) 1 else time.hour + 1
                onTimeChange(time.copy(hour = next))
            },
            onDecrement = {
                val prev = if (time.hour <= 1) 12 else time.hour - 1
                onTimeChange(time.copy(hour = prev))
            },
        )

        TimeSeparator()

        // Minute spinner (5-min steps)
        TimeSpinnerCell(
            value = time.minute.toString().padStart(2, '0'),
            onIncrement = {
                val next = (time.minute + 5) % 60
                onTimeChange(time.copy(minute = next))
            },
            onDecrement = {
                val prev = (time.minute - 5 + 60) % 60
                onTimeChange(time.copy(minute = prev))
            },
        )

        Spacer(Modifier.width(16.dp))

        // AM / PM toggle
        AmPmToggle(
            isPm = time.isPm,
            onToggle = { isPm -> onTimeChange(time.copy(isPm = isPm)) },
        )
    }
}

@Composable
private fun TimeSpinnerCell(
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numGradient = Brush.verticalGradient(listOf(DwWaterLight, DwWater, DwWaterDeep))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onIncrement, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = ChevronUp,
                contentDescription = "Increment",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(numGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
        }

        IconButton(onClick = onDecrement, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = ChevronDown,
                contentDescription = "Decrement",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun AmPmToggle(
    isPm: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        listOf(false to "AM", true to "PM").forEach { (pm, label) ->
            val active = isPm == pm
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (active) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.secondaryContainer
                        else Color.Transparent,
                    )
                    .clickable { onToggle(pm) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}


@DwPreview
@Composable
private fun OnbTimeWheelPreview() {
    DrinkWaterAppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            OnbTimeWheel(
                time = OnbTimeSelection(hour = 7, minute = 0, isPm = false),
                onTimeChange = {},
            )
        }
    }
}
