package com.eahm.drinkwaterapp.ui.drinkbutton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eahm.drinkwaterapp.ui.PulsingRippleButton
import com.eahm.drinkwaterapp.ui.RippleButtonConfig
import com.eahm.drinkwaterapp.ui.RippleFadeCurve

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkButton(
    text: String,
    isGoalReached: Boolean = false,
    onClick: (Float) -> Unit,
) {
    var showModal by rememberSaveable { mutableStateOf(false) }

    val config = if (isGoalReached) {
        val brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD700),
                Color(0xFFD1A41F),
            ),
            radius = 150f
        )

        RippleButtonConfig(
            buttonSizeDp = 130.dp,
            rippleFrequencyMs = 500L,
            maxRipples = 8,
            fadeCurve = RippleFadeCurve.EaseOutStrong,
            buttonBrush = brush,
            textColor = Color.White,
            rippleColorInner = Color.Yellow,
            rippleColorOuter = Color.Yellow,
        )
    } else {
        RippleButtonConfig(
            buttonSizeDp = 130.dp,
            rippleFrequencyMs = 500L,
            maxRipples = 8,
            fadeCurve = RippleFadeCurve.EaseOutStrong,
            buttonColor = Color(0xFF4DAEFF),
            textColor = Color.White,
            rippleColorInner = Color(0xFF5EC8E8),
            rippleColorOuter = Color(0xFF2196F3),
        )
    }

    PulsingRippleButton(
        text = text,
        onClick = { showModal = !showModal },
        config = config,
    )

    if (showModal) {
        DrinkAmountOptions(
            onAmountSelected = { onClick(it) },
            onDismiss = { showModal = false },
        )
    }
}

@Preview
@Composable
private fun DrinkButtonPreview() {
    DrinkButton(
        text = "Time",
        onClick = {},
    )
}

@Composable
fun DrinkAmountOptions(
    onDismiss: () -> Unit,
    onAmountSelected: (Float) -> Unit,
) {
    val default = listOf(
        Pair(100.0f, "ml"),
        Pair(200.0f, "ml"),
        Pair(300.0f, "ml"),
        Pair(400.0f, "ml"),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        ),
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(12.dp),
            ) {
                Text(
                    text = "Cuanto has bebido",
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(30.dp),
                ) {
                    items(default) { (amount, unit) ->
                        Card(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(7.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 10.dp,
                            ),
                            onClick = {
                                onAmountSelected(amount)
                                onDismiss()
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(30.dp),
                                text = "$amount$unit",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = "Delete/Cancel",
                        )
                    }

                    Button(
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = "Create new",
                        )
                    }
                }
            }
        }
    }
}
