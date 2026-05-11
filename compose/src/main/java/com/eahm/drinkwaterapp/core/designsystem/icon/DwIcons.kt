package com.eahm.drinkwaterapp.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object DwIcons {

    val Drop: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwDrop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                curveToRelative(-4f, 5f, -7f, 9f, -7f, 12f)
                arcToRelative(7f, 7f, 0f, false, false, 14f, 0f)
                curveToRelative(0f, -3f, -3f, -7f, -7f, -12f)
                close()
            }
        }.build()
    }

    val Wave: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwWave",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 17f)
                quadToRelative(3f, -4f, 6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeAlpha = 0.55f
            ) {
                moveTo(3f, 12f)
                quadToRelative(3f, -4f, 6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                lineToRelative(1.6f, 1.6f)
                horizontalLineToRelative(2.2f)
                lineToRelative(0.6f, 2.2f)
                lineToRelative(2.2f, 0.6f)
                verticalLineToRelative(2.2f)
                lineToRelative(1.6f, 1.6f)
                lineToRelative(-1.6f, 1.6f)
                verticalLineToRelative(2.2f)
                lineToRelative(-2.2f, 0.6f)
                lineToRelative(-0.6f, 2.2f)
                horizontalLineToRelative(-2.2f)
                lineToRelative(-1.6f, 1.6f)
                lineToRelative(-1.6f, -1.6f)
                horizontalLineToRelative(-2.2f)
                lineToRelative(-0.6f, -2.2f)
                lineToRelative(-2.2f, -0.6f)
                verticalLineToRelative(-2.2f)
                lineToRelative(-1.6f, -1.6f)
                lineToRelative(1.6f, -1.6f)
                verticalLineToRelative(-2.2f)
                lineToRelative(2.2f, -0.6f)
                lineToRelative(0.6f, -2.2f)
                horizontalLineToRelative(2.2f)
                close()
            }
            // Inner circle
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(15f, 12f)
                arcTo(3f, 3f, 0f, false, false, 9f, 12f)
                arcTo(3f, 3f, 0f, false, false, 15f, 12f)
                close()
            }
        }.build()
    }

    val Glass: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwGlass",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 4f)
                horizontalLineTo(17f)
                lineToRelative(-1f, 16f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(10f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(7.4f, 9f)
                horizontalLineTo(16.6f)
            }
        }.build()
    }

    val Star: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwStar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                strokeLineWidth = 0f
            ) {
                moveTo(12f, 2f)
                lineTo(15f, 9f)
                lineTo(22f, 9.5f)
                lineTo(17f, 14.5f)
                lineTo(18.5f, 22f)
                lineTo(12f, 18f)
                lineTo(5.5f, 22f)
                lineTo(7f, 14.5f)
                lineTo(2f, 9.5f)
                lineTo(9f, 9f)
                close()
            }
        }.build()
    }

    val Plus: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwPlus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 5f)
                verticalLineTo(19f)
                moveTo(5f, 12f)
                horizontalLineTo(19f)
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9f, 18f)
                lineTo(15f, 12f)
                lineTo(9f, 6f)
            }
        }.build()
    }

    val Bell: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwBell",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 8f)
                arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
                curveToRelative(0f, 7f, 3f, 9f, 3f, 9f)
                horizontalLineTo(3f)
                reflectiveCurveToRelative(3f, -2f, 3f, -9f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(10.3f, 21f)
                arcToRelative(1.94f, 1.94f, 0f, false, false, 3.4f, 0f)
            }
        }.build()
    }

    val Clock: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwClock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                curveToRelative(5.523f, 0f, 10f, 4.477f, 10f, 10f)
                curveToRelative(0f, 5.523f, -4.477f, 10f, -10f, 10f)
                curveToRelative(-5.523f, 0f, -10f, -4.477f, -10f, -10f)
                curveToRelative(0f, -5.523f, 4.477f, -10f, 10f, -10f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 6f)
                lineTo(12f, 12f)
                lineTo(16f, 14f)
            }
        }.build()
    }

    val Sun: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwSun",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 8f)
                curveToRelative(2.209f, 0f, 4f, 1.791f, 4f, 4f)
                curveToRelative(0f, 2.209f, -1.791f, 4f, -4f, 4f)
                curveToRelative(-2.209f, 0f, -4f, -1.791f, -4f, -4f)
                curveToRelative(0f, -2.209f, 1.791f, -4f, 4f, -4f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 2f); lineTo(12f, 4f)
                moveTo(12f, 20f); lineTo(12f, 22f)
                moveTo(2f, 12f); lineTo(4f, 12f)
                moveTo(20f, 12f); lineTo(22f, 12f)
                moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
                moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
                moveTo(4.93f, 19.07f); lineTo(6.34f, 17.66f)
                moveTo(17.66f, 6.34f); lineTo(19.07f, 4.93f)
            }
        }.build()
    }

    val Moon: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwMoon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                arcToRelative(6f, 6f, 0f, false, false, 9f, 9f)
                arcToRelative(9f, 9f, 0f, true, true, -9f, -9f)
                close()
            }
        }.build()
    }

    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwShare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 12f)
                verticalLineToRelative(8f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineToRelative(12f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineToRelative(-8f)
                moveTo(16f, 6f)
                lineToRelative(-4f, -4f)
                lineToRelative(-4f, 4f)
                moveTo(12f, 2f)
                verticalLineTo(15f)
            }
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwHistory",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Almost-complete clock arc, leaving a gap at upper-left where the arrow emerges.
            // First arc: large CCW sweep from (3,12) to (12,3) — most of the circle.
            // Second arc: small CCW arc that curves into the arrow tip at (3,8).
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(3f, 12f)
                arcToRelative(
                    9f, 9f, 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 9f,
                    dy1 = -9f,
                )
                arcToRelative(
                    9.75f, 9.75f, 0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.74f,
                    dy1 = 2.74f,
                )
                lineTo(3f, 8f)
            }
            // Counter-clockwise L-shaped arrow in the upper-left gap.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 3f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(5f)
            }
            // Clock hands: 12 o'clock → center → ~2 o'clock.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 7f)
                verticalLineTo(12f)
                lineTo(16f, 14f)
            }
        }.build()
    }

    val Trash: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwTrash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Lid line
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(3f, 6f)
                horizontalLineTo(21f)
            }
            // Handle tab
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(8f, 6f)
                verticalLineTo(4f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                horizontalLineToRelative(6f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
                verticalLineTo(6f)
            }
            // Rounded body
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(19f, 6f)
                lineToRelative(-1f, 13f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(8f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                lineToRelative(-1f, -13f)
            }
            // Internal lines
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeAlpha = 0.4f,
            ) {
                moveTo(9f, 10f); verticalLineTo(17f)
                moveTo(12f, 10f); verticalLineTo(17f)
                moveTo(15f, 10f); verticalLineTo(17f)
            }
        }.build()
    }

    val Flag: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwFlag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Flagpole
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 21f)
                verticalLineTo(3f)
            }
            // Flag shape
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 3f)
                lineTo(18f, 8f)
                lineTo(4f, 13f)
                close()
            }
        }.build()
    }

    val Lightbulb: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwLightbulb",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Bulb body
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                curveToRelative(-3.866f, 0f, -7f, 3.134f, -7f, 7f)
                curveToRelative(0f, 2.386f, 1.2f, 4.485f, 3f, 5.745f)
                verticalLineTo(17f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineToRelative(6f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                verticalLineTo(15.745f)
                curveToRelative(1.8f, -1.26f, 3f, -3.359f, 3f, -5.745f)
                curveToRelative(0f, -3.866f, -3.134f, -7f, -7f, -7f)
                close()
            }
            // Base lines
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(9f, 21f)
                horizontalLineTo(15f)
            }
        }.build()
    }

    val Ruler: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwRuler",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 8f)
                lineTo(10f, 3f)
                lineTo(21f, 14f)
                lineTo(16f, 19f)
                close()
                moveTo(7.5f, 10.5f); lineTo(10f, 8f)
                moveTo(10.5f, 13.5f); lineTo(13f, 11f)
                moveTo(13.5f, 16.5f); lineTo(16f, 14f)
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(18f, 6f); lineTo(6f, 18f)
                moveTo(6f, 6f); lineTo(18f, 18f)
            }
        }.build()
    }
}
