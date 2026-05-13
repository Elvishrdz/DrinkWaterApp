package com.eahm.drinkwaterapp.system.service.goalcelebration.ui

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk500
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagenta
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CELEBRATIONS
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationMark
import com.eahm.drinkwaterapp.system.service.goalcelebration.data.CelebrationVariant
import kotlinx.coroutines.launch

@Composable
fun GoalCelebrationCard(
    variant: CelebrationVariant,
    goalMl: Float,
    drinkCount: Int,
    streakDays: Int,
    onClose: () -> Unit,
    onShare: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    showStreakDays: Boolean = true,
    showShareProgressButton: Boolean = true,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    // Medallion pop-in animation
    var medallionVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { medallionVisible = true }

    val medallionScale by animateFloatAsState(
        targetValue = if (medallionVisible) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "medallion_scale",
    )
    val medallionRotation by animateFloatAsState(
        targetValue = if (medallionVisible) 0f else -12f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "medallion_rotation",
    )

    Card(
        modifier = modifier
            .drawWithContent {
                // Captured region: all card
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }
            .wrapContentSize()
            .widthIn(max = 320.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column {
            Column {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(variant.heroBrush)
                ) {
                    // Optional overlay brush (STARLIGHT / MOONLIT) fills full hero
                    variant.heroOverlayBrush?.let { overlay ->
                        Box(
                            modifier = Modifier
                                .wrapContentSize()
                                .background(overlay)
                        )
                    }

                    // Particle decor layer (absolute, fills full hero, no pointer events)
                    DecorLayer(
                        decorKind = variant.decorKind,
                        modifier = Modifier
                            .matchParentSize(),
                    )

                    // Glass shine layer top-left diagonal highlight
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.10f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    )

                    // Hero content column padding lives here so decor fills the full hero
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val medallionShape = CircleShape
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(medallionScale)
                                .rotate(medallionRotation)
                                .background(
                                    Color.White.copy(alpha = 0.24f),
                                    medallionShape,
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.34f),
                                    shape = medallionShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            // background shadow
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(width = 23.dp, height = 49.dp)
                                    .dropShadow(
                                        shape = RectangleShape,
                                        shadow = Shadow(
                                            radius = 5.dp,
                                            spread = 5.dp,
                                            color = Color.Black.copy(alpha = 0.07f),
                                            offset = DpOffset(x = 0.dp, y = 3.dp),
                                        )
                                    )
                            )

                            when (val m = variant.mark) {
                                is CelebrationMark.Image -> Image(
                                    painter = painterResource(id = m.resId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp),
                                )

                                is CelebrationMark.Glyph -> Text(
                                    text = m.text,
                                    fontSize = 44.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        val eyebrowShape = RoundedCornerShape(100)
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.24f),
                                    shape = eyebrowShape,
                                )
                                .padding(horizontal = 10.dp),
                        ) {
                            Text(
                                text = variant.eyebrow.uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                            )
                        }

                        // Headline
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            text = variant.headline,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 30.sp,
                            textAlign = TextAlign.Center,
                        )

                        // Sub copy
                        Text(
                            text = variant.sub,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // Close button (top-right aligned)
                    Box(
                        modifier = Modifier
                            .padding(top = 28.dp, end = 24.dp)
                            .fillMaxWidth(),
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White,
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Close",
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                // ── Stats strip ──────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatCell(
                            value = "%.1f".format(goalMl / 1000f),
                            unit = "L",
                            label = "Reached",
                            modifier = Modifier.weight(1f),
                        )
                        StatCell(
                            value = "$drinkCount",
                            unit = "",
                            label = "Glasses",
                            modifier = Modifier.weight(1f),
                        )
                        if (showStreakDays) {
                            StatCell(
                                value = "$streakDays",
                                unit = "d",
                                label = "Streak",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }
            }

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DwPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = variant.cta,
                    onClick = onClose,
                )

                if (showShareProgressButton) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            onShare(graphicsLayer.toImageBitmap().asAndroidBitmap())
                        }
                    }) {
                        Text(
                            text = "Share progress",
                            color = DwInk500,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DwMagenta,
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DwMagenta,
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B8290),
        )
    }
}

@Preview(name = "Ocean")
@Composable
private fun PreviewOcean() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[0],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Carnival")
@Composable
private fun PreviewCarnival() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[1],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Starlight")
@Composable
private fun PreviewStarlight() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[2],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Rain")
@Composable
private fun PreviewRain() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[3],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Sunset")
@Composable
private fun PreviewSunset() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[4],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, name = "Moonlit")
@Composable
private fun PreviewMoonlit() {
    DrinkWaterAppTheme {
        GoalCelebrationCard(
            variant = CELEBRATIONS[5],
            goalMl = 2400f,
            drinkCount = 8,
            streakDays = 3,
            onClose = {},
            onShare = {},
        )
    }
}
