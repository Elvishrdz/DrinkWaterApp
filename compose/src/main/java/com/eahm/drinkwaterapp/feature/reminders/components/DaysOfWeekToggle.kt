@file:Suppress("MagicNumber")

package com.eahm.drinkwaterapp.feature.reminders.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Horizontal row of 7 day-initial buttons (Mon–Sun).
 *
 * Day labels adapt to the current locale automatically via [TextStyle.NARROW] formatting.
 *
 * Active button: filled with a radial gradient built from the theme primary colour.
 * Inactive button: transparent background with a themed border.
 *
 * Layout adapts to the available width: a [FlowRow] keeps the 7 buttons on a single line when they
 * fit and wraps the overflow onto a second line when space is tight (e.g. inside a Settings row on a
 * narrow phone).
 *
 * When [centered] is true the toggle fills available space up to 480 dp and centres itself on wider
 * screens (standalone Reminders screen / tablets). When false it hugs the start, which suits an
 * inline placement inside a Settings row.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DaysOfWeekToggle(
    activeDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
) {
    val alignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    val widthModifier = if (centered) {
        Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally, unbounded = false)
            .widthIn(max = 480.dp)
    } else {
        Modifier.fillMaxWidth()
    }
    FlowRow(
        modifier = modifier.then(widthModifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ISO week: 1=Mon … 7=Sun
        for (isoDay in 1..7) {
            DayButton(
                isoDay = isoDay,
                isActive = isoDay in activeDays,
                onClick = { onDayToggled(isoDay) },
            )
        }
    }
}

@Composable
private fun DayButton(
    isoDay: Int,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val label = dayNarrowLabel(isoDay)

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "day_scale_$isoDay",
    )

    // Gradient for active state: primary at centre → secondary at edge.
    val activeBrush = Brush.radialGradient(
        colors = listOf(Color(0xFFB83A95), DwMagentaDark),
    )

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (isActive) {
                    Modifier.background(brush = activeBrush, shape = CircleShape)
                } else {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    )
                }
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Checkbox
                contentDescription = label
                stateDescription = if (isActive) "active" else "inactive"
            },
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontFamily = Nunito,
            color = contentColor,
        )
    }
}

private fun dayNarrowLabel(isoDay: Int): String =
    DayOfWeek.of(isoDay).getDisplayName(TextStyle.NARROW, Locale.getDefault())

@Preview(showBackground = true)
@Composable
private fun DaysOfWeekTogglePreview() {
    DrinkWaterAppTheme {
        DaysOfWeekToggle(
            activeDays = setOf(1, 2, 3, 4, 5),
            onDayToggled = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun DaysOfWeekToggleWidePreview() {
    DrinkWaterAppTheme {
        DaysOfWeekToggle(
            activeDays = setOf(1, 3, 5),
            onDayToggled = {},
        )
    }
}
