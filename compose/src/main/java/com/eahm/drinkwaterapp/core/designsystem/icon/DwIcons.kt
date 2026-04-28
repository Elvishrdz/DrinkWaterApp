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
}
