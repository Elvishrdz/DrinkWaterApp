package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.core.designsystem.component.extension.outlineBorderStyle
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk400
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper
import com.eahm.drinkwaterapp.core.designsystem.theme.DwWater
import com.eahm.drinkwaterapp.core.designsystem.theme.JetBrainsMono
import com.eahm.drinkwaterapp.core.designsystem.theme.Nunito
import com.eahm.drinkwaterapp.core.designsystem.theme.PlusJakartaSans
import com.eahm.drinkwaterapp.ui.inputfield.toDigitString

data class DrinkLogEntry(
    val time: String,
    val amount: Float,
    val timestamp: String,
)

@Composable
fun TodayLogSection(
    items: List<DrinkLogEntry>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .outlineBorderStyle()
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(
                onClick = onSeeAll,
                contentPadding = PaddingValues(
                    horizontal = 4.dp,
                    vertical = 0.dp
                ),
            ) {
                Text(
                    text = "SEE ALL",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.05.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (items.isEmpty()) {
            Text(
                text = "No drinks logged yet — tap Add water to start.",
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                color = DwInk400,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        } else {
            // Keys present at first composition — these entries appear instantly with no animation.
            // Items that arrive later (not in this set) animate in with expandVertically + fadeIn.
            val initialKeys = remember { items.map { it.timestamp }.toSet() }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEachIndexed { index, entry ->
                    val entryKey = entry.timestamp
                    key(entryKey) {
                        // Initial items start already visible (no animation).
                        // New items start invisible with targetState=true so the
                        // transition fires immediately on first composition.
                        val isInitial = remember { entryKey in initialKeys }
                        val transitionState = remember {
                            MutableTransitionState(isInitial).also {
                                if (!isInitial) it.targetState = true
                            }
                        }
                        AnimatedVisibility(
                            visibleState = transitionState,
                            enter = expandVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                                expandFrom = Alignment.Top,
                            ) + fadeIn(animationSpec = spring()),
                        ) {
                            Column {
                                DrinkLogRow(entry = entry)
                                if (index < items.lastIndex) {
                                    HorizontalDivider(
                                        color = DwInk100,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrinkLogRow(entry: DrinkLogEntry) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DwWater)
        ) {
            Icon(
                imageVector = DwIcons.Glass,
                contentDescription = null,
                tint = DwPaper,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "${entry.amount.toDigitString()} mL",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        Text(
            text = entry.time,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = DwInk400,
        )
    }
}

val previewLogEntries = listOf(
    DrinkLogEntry("09:14", 250f, "1"),
    DrinkLogEntry("10:30", 500f, "2"),
    DrinkLogEntry("11:45", 100f, "3"),
    DrinkLogEntry("13:02", 250f, "4"),
    DrinkLogEntry("14:20", 400f, "5"),
)

@Preview
@Composable
private fun TodayLogSectionPreview() {
    DrinkWaterAppTheme {
        TodayLogSection(
            items = previewLogEntries,
            onSeeAll = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun TodayLogSectionEmptyPreview() {
    DrinkWaterAppTheme {
        TodayLogSection(
            items = emptyList(),
            onSeeAll = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
