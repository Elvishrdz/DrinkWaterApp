package com.eahm.drinkwaterapp.system.service.goalcelebration.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eahm.drinkwaterapp.R

enum class DecorKind { BUBBLES, CONFETTI, SPARKLES, DRIPS, RAYS, RINGS }

sealed class CelebrationMark {
    data class Image(@DrawableRes val resId: Int) : CelebrationMark()
    data class Glyph(val text: String) : CelebrationMark()
}

data class CelebrationVariant(
    val id: String,
    val heroBrush: Brush,
    val heroOverlayBrush: Brush? = null,
    val decorKind: DecorKind,
    val mark: CelebrationMark,
    val eyebrow: String,
    val headline: String,
    val sub: String,
    val cta: String,
)

enum class CelebrationStyle {
    Ocean,
    Carnival,
}

val oceanStyle = CelebrationVariant(
    id = "ocean",
    heroBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF4FB3DD),
            0.5f to Color(0xFF1C8EBF),
            1.0f to Color(0xFF0E5A82),
        )
    ),
    decorKind = DecorKind.BUBBLES,
    mark = CelebrationMark.Image(R.drawable.icon_trophie_white),
    eyebrow = "• Goal Day 1", //"•••••• Daily Goal",
    headline = "You did it. Today belongs to your future self.",// "Goal Reached!",
    sub = "Every glass added up, and so did the focus, the energy, the calm.", //"You hit your hydration target for today. Your body thanks you — keep it up!",
    cta = "Cheers to that", // "Keep Going",
)
val carnivalStyle = CelebrationVariant(
    id = "carnival",
    heroBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB83A95),
            Color(0xFF902178),
        )
    ),
    decorKind = DecorKind.CONFETTI,
    mark = CelebrationMark.Glyph("🎉"),
    eyebrow = "•••••• Daily Goal",
    headline = "You Did It!",
    sub = "Celebration time! You've reached your daily water goal. Amazing work!",
    cta = "Celebrate",
)

val starlightStyle = CelebrationVariant(
    id = "starlight",
    heroBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0E2235),
            Color(0xFF0B1C2C),
        )
    ),
    heroOverlayBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1C8EBF).copy(alpha = 0.30f),
            Color.Transparent,
        ),
        center = Offset(0.5f, 0.2f),
        radius = 300f,
    ),
    decorKind = DecorKind.SPARKLES,
    mark = CelebrationMark.Image(R.drawable.icon_trophie_yellow),
    eyebrow = "•••••• Daily Goal",
    headline = "Star Performer!",
    sub = "Like the stars above, you shine bright. Your daily goal is complete!",
    cta = "Keep Shining",
)
val rainStyle = CelebrationVariant(
    id = "rain",
    heroBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2BA89A),
            Color(0xFF1C8EBF),
            Color(0xFF0E5A82),
        )
    ),
    decorKind = DecorKind.DRIPS,
    mark = CelebrationMark.Image(R.drawable.icon_glass_of_water_01),
    eyebrow = "•••••• Daily Goal",
    headline = "Fully Hydrated!",
    sub = "Every drop counts! You've completed your hydration goal for today.",
    cta = "Stay Hydrated",
)
val sunsetStyle = CelebrationVariant(
    id = "sunset",
    heroBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4FB3DD),
            Color(0xFF1C8EBF),
            Color(0xFFA22C83),
        ),
        start = Offset(0f, 0f),
        end = Offset(0.34f, 1f),
    ),
    decorKind = DecorKind.RAYS,
    mark = CelebrationMark.Glyph("✦"),
    eyebrow = "•••••• Daily Goal",
    headline = "Golden Hour!",
    sub = "You've reached your peak hydration! Sunset and goal complete — well done.",
    cta = "Awesome",
)
val moonlitStyle = CelebrationVariant(
    id = "moonlit",
    heroBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF008577),
            Color(0xFF00574B),
        )
    ),
    heroOverlayBrush = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.Transparent,
        ),
        center = Offset(0.8f, 0.15f),
        radius = 250f,
    ),
    decorKind = DecorKind.RINGS,
    mark = CelebrationMark.Glyph("☾"),
    eyebrow = "•••••• Daily Goal",
    headline = "Moonlit Victory!",
    sub = "Under the moonlit sky, you completed your daily hydration mission!",
    cta = "Wonderful",
)
val CELEBRATIONS: List<CelebrationVariant> by lazy {
    listOf(
        oceanStyle,
        carnivalStyle,
        starlightStyle,
        rainStyle,
        sunsetStyle,
        moonlitStyle,
    )
}

fun getStyle(value: CelebrationStyle?): CelebrationVariant {
    return when (value) {
        CelebrationStyle.Ocean -> oceanStyle
        CelebrationStyle.Carnival -> carnivalStyle
        null -> CELEBRATIONS.random()
    }
}
