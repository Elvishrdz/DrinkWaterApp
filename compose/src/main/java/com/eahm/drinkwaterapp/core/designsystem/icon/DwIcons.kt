package com.eahm.drinkwaterapp.core.designsystem.icon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons.Settings
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

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
            // 8-tooth gear: outer r=10, inner r=7.5.
            // Each tooth spans 16° at outer radius; each valley spans 29° at inner radius.
            // Coordinates computed at tooth-edge angles: ±8° per tooth, ±7.5° spacing at valley.
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(21.90f, 10.61f)                               // outer −8°  (tooth 0 left)
                arcTo(10f, 10f, 0f, false, true, 21.90f, 13.39f)    // tooth 0 top  (−8° → +8°)
                lineTo(19.43f, 13.04f)                               // drop → inner +8°
                arcTo(7.5f, 7.5f, 0f, false, true, 17.99f, 16.51f) // valley        (+8° → +37°)
                lineTo(19.99f, 18.02f)                               // rise → outer +37°
                arcTo(10f, 10f, 0f, false, true, 18.02f, 19.99f)    // tooth 1 top  (+37° → +53°)
                lineTo(16.51f, 17.99f)                               // drop → inner +53°
                arcTo(7.5f, 7.5f, 0f, false, true, 13.04f, 19.43f) // valley       (+53° → +82°)
                lineTo(13.39f, 21.90f)                               // rise → outer +82°
                arcTo(10f, 10f, 0f, false, true, 10.61f, 21.90f)    // tooth 2 top  (+82° → +98°)
                lineTo(10.96f, 19.43f)                               // drop → inner +98°
                arcTo(7.5f, 7.5f, 0f, false, true, 7.49f, 17.99f)  // valley       (+98° → +127°)
                lineTo(5.98f, 19.99f)                                // rise → outer +127°
                arcTo(10f, 10f, 0f, false, true, 4.01f, 18.02f)     // tooth 3 top (+127° → +143°)
                lineTo(6.01f, 16.51f)                                // drop → inner +143°
                arcTo(7.5f, 7.5f, 0f, false, true, 4.57f, 13.04f)  // valley      (+143° → +172°)
                lineTo(2.10f, 13.39f)                                // rise → outer +172°
                arcTo(10f, 10f, 0f, false, true, 2.10f, 10.61f)     // tooth 4 top (+172° → +188°)
                lineTo(4.57f, 10.96f)                                // drop → inner +188°
                arcTo(7.5f, 7.5f, 0f, false, true, 6.01f, 7.49f)   // valley      (+188° → +217°)
                lineTo(4.01f, 5.98f)                                 // rise → outer +217°
                arcTo(10f, 10f, 0f, false, true, 5.98f, 4.01f)      // tooth 5 top (+217° → +233°)
                lineTo(7.49f, 6.01f)                                 // drop → inner +233°
                arcTo(7.5f, 7.5f, 0f, false, true, 10.96f, 4.57f)  // valley      (+233° → +262°)
                lineTo(10.61f, 2.10f)                                // rise → outer +262°
                arcTo(10f, 10f, 0f, false, true, 13.39f, 2.10f)     // tooth 6 top (+262° → +278°)
                lineTo(13.04f, 4.57f)                                // drop → inner +278°
                arcTo(7.5f, 7.5f, 0f, false, true, 16.51f, 6.01f)  // valley      (+278° → +307°)
                lineTo(18.02f, 4.01f)                                // rise → outer +307°
                arcTo(10f, 10f, 0f, false, true, 19.99f, 5.98f)     // tooth 7 top (+307° → +323°)
                lineTo(17.99f, 7.49f)                                // drop → inner +323°
                arcTo(7.5f, 7.5f, 0f, false, true, 19.43f, 10.96f) // valley      (+323° → +352°)
                close()                                              // left face of tooth 0
            }
            // Center circle
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.5f,
            ) {
                moveTo(15.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, false, false, 8.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, false, false, 15.5f, 12f)
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

    // ── Filled nav variants ───────────────────────────────────────────────────

    /** Filled water drop — used as the selected-state icon for the Today nav item. */
    val DropFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwDropFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                curveToRelative(-4f, 5f, -7f, 9f, -7f, 12f)
                arcToRelative(7f, 7f, 0f, false, false, 14f, 0f)
                curveToRelative(0f, -3f, -3f, -7f, -7f, -12f)
                close()
            }
        }.build()
    }

    /**
     * Bold wave — used as the selected-state icon for the Stats nav item.
     * Both wave lines are drawn at full alpha with a heavier stroke weight.
     */
    val WaveFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwWaveFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 3f,
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
                strokeLineWidth = 3f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeAlpha = 0.6f
            ) {
                moveTo(3f, 12f)
                quadToRelative(3f, -4f, 6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
                reflectiveQuadToRelative(6f, 0f)
            }
        }.build()
    }

    /**
     * Filled clock face with cutout clock hands — selected-state icon for History.
     * Uses EvenOdd fill rule so the hand cutouts are transparent against any background.
     */
    val HistoryFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwHistoryFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                // Full circle (r=10, center (12,12)): right half then left half, both CW
                moveTo(12f, 2f)
                arcToRelative(10f, 10f, 0f, false, true, 0f, 20f)
                arcToRelative(10f, 10f, 0f, false, true, 0f, -20f)
                close()
                // Vertical hand (12 o'clock → center): thin filled rectangle cutout
                moveTo(11.3f, 6.5f)
                lineTo(12.7f, 6.5f)
                lineTo(12.7f, 12.8f)
                lineTo(11.3f, 12.8f)
                close()
                // Diagonal hand (center → ~2 o'clock): rotated rectangle cutout
                moveTo(11.71f, 12.58f)
                lineTo(12.29f, 11.42f)
                lineTo(16.29f, 13.42f)
                lineTo(15.71f, 14.58f)
                close()
            }
        }.build()
    }

    /**
     * Filled gear with a transparent center-hole cutout — selected-state icon for Settings.
     * Same 8-tooth geometry as [Settings]; uses EvenOdd fill rule for the center cutout.
     */
    val SettingsFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwSettingsFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                // Gear body — same 8-tooth path as Settings
                moveTo(21.90f, 10.61f)
                arcTo(10f, 10f, 0f, false, true, 21.90f, 13.39f)
                lineTo(19.43f, 13.04f)
                arcTo(7.5f, 7.5f, 0f, false, true, 17.99f, 16.51f)
                lineTo(19.99f, 18.02f)
                arcTo(10f, 10f, 0f, false, true, 18.02f, 19.99f)
                lineTo(16.51f, 17.99f)
                arcTo(7.5f, 7.5f, 0f, false, true, 13.04f, 19.43f)
                lineTo(13.39f, 21.90f)
                arcTo(10f, 10f, 0f, false, true, 10.61f, 21.90f)
                lineTo(10.96f, 19.43f)
                arcTo(7.5f, 7.5f, 0f, false, true, 7.49f, 17.99f)
                lineTo(5.98f, 19.99f)
                arcTo(10f, 10f, 0f, false, true, 4.01f, 18.02f)
                lineTo(6.01f, 16.51f)
                arcTo(7.5f, 7.5f, 0f, false, true, 4.57f, 13.04f)
                lineTo(2.10f, 13.39f)
                arcTo(10f, 10f, 0f, false, true, 2.10f, 10.61f)
                lineTo(4.57f, 10.96f)
                arcTo(7.5f, 7.5f, 0f, false, true, 6.01f, 7.49f)
                lineTo(4.01f, 5.98f)
                arcTo(10f, 10f, 0f, false, true, 5.98f, 4.01f)
                lineTo(7.49f, 6.01f)
                arcTo(7.5f, 7.5f, 0f, false, true, 10.96f, 4.57f)
                lineTo(10.61f, 2.10f)
                arcTo(10f, 10f, 0f, false, true, 13.39f, 2.10f)
                lineTo(13.04f, 4.57f)
                arcTo(7.5f, 7.5f, 0f, false, true, 16.51f, 6.01f)
                lineTo(18.02f, 4.01f)
                arcTo(10f, 10f, 0f, false, true, 19.99f, 5.98f)
                lineTo(17.99f, 7.49f)
                arcTo(7.5f, 7.5f, 0f, false, true, 19.43f, 10.96f)
                close()
                // Center hole — EvenOdd makes this transparent
                moveTo(15.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, false, false, 8.5f, 12f)
                arcTo(3.5f, 3.5f, 0f, false, false, 15.5f, 12f)
                close()
            }
        }.build()
    }

    val ShieldCheck: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwShieldCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            // Shield body — 10 % fill so the tint colour shows as a subtle glow
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.10f,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(16f, 4f)
                lineTo(26f, 8f)
                verticalLineTo(16f)
                curveTo(26f, 22f, 21.5f, 26.5f, 16f, 28f)
                curveTo(10.5f, 26.5f, 6f, 22f, 6f, 16f)
                verticalLineTo(8f)
                close()
            }
            // Checkmark
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 16f)
                lineTo(15f, 19f)
                lineTo(21f, 13f)
            }
        }.build()
    }

    val PulseHeart: ImageVector by lazy {
        ImageVector.Builder(
            name = "DwPulseHeart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            // Heart body — 10 % fill
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 0.10f,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(16f, 27f)
                curveTo(8f, 21f, 4f, 16f, 7f, 11f)
                curveTo(10f, 6f, 15f, 7f, 16f, 11f)
                curveTo(17f, 7f, 22f, 6f, 25f, 11f)
                curveTo(28f, 16f, 24f, 21f, 16f, 27f)
                close()
            }
            // EKG / pulse line
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(7f, 16f)
                horizontalLineTo(11f)
                lineTo(13f, 12f)
                lineTo(16f, 20f)
                lineTo(18f, 14f)
                lineTo(20f, 16f)
                horizontalLineTo(25f)
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

@Preview(name = "DwIcon", showBackground = true, backgroundColor = 0xFFF2F6F9)
@Composable
private fun DwIconsPreview(
    @PreviewParameter(DwIconsPreviewProvider::class) item: DwIconSample,
) {
    DrinkWaterAppTheme {
        Surface {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = item.vector,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
