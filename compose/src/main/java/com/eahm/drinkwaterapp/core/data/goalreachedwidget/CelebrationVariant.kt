package com.eahm.drinkwaterapp.core.data.goalreachedwidget

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

val CELEBRATIONS: List<CelebrationVariant> by lazy {
    listOf(
        // 0 — ocean
        CelebrationVariant(
            id = "ocean",
            heroBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4FB3DD),
                    Color(0xFF1C8EBF),
                    Color(0xFF0E5A82),
                )
            ),
            decorKind = DecorKind.BUBBLES,
            mark = CelebrationMark.Image(R.drawable.icon_trophie_white),
            eyebrow = "•••••• Daily Goal",
            headline = "Goal Reached!",
            sub = "You hit your hydration target for today. Your body thanks you — keep it up!",
            cta = "Keep Going",
        ),
        // 1 — carnival
        CelebrationVariant(
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
        ),
        // 2 — starlight
        CelebrationVariant(
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
        ),
        // 3 — rain
        CelebrationVariant(
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
        ),
        // 4 — sunset
        CelebrationVariant(
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
        ),
        // 5 — moonlit
        CelebrationVariant(
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
        ),
    )
}

fun todayVariant(): CelebrationVariant = CELEBRATIONS.random()
