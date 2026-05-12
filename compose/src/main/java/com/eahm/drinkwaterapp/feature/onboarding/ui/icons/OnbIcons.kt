package com.eahm.drinkwaterapp.feature.onboarding.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Onboarding-specific icons not present in DwIcons. */
internal object OnbIcons {

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbArrowBack",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(19f, 12f); horizontalLineTo(5f)
                moveTo(12f, 19f); lineTo(5f, 12f); lineTo(12f, 5f)
            }
        }.build()
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbPerson",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 4f)
                arcToRelative(
                    4f,
                    4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = 8f
                )
                arcToRelative(
                    4f,
                    4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = -8f
                )
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 21f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 16f,
                    dy1 = 0f
                )
            }
        }.build()
    }

    val Scale: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbScale",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            // Scale platform base
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 5f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2f,
                    dy1 = -2f
                )
                horizontalLineTo(18f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2f,
                    dy1 = 2f
                )
                verticalLineTo(17f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2f,
                    dy1 = 2f
                )
                horizontalLineTo(6f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2f,
                    dy1 = -2f
                )
                close()
            }
            // Display circle
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(16f, 11f)
                arcToRelative(
                    4f,
                    4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -8f,
                    dy1 = 0f
                )
                arcToRelative(
                    4f,
                    4f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 8f,
                    dy1 = 0f
                )
                close()
            }
            // Needle
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(12f, 7f); verticalLineToRelative(4f)
            }
        }.build()
    }

    val ActivityRun: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbActivityRun",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            // Head
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(15f, 5f)
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4f,
                    dy1 = 0f
                )
                arcToRelative(
                    2f,
                    2f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4f,
                    dy1 = 0f
                )
                close()
            }
            // Body / stride
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(8f, 20f)
                lineToRelative(3f, -6f)
                lineToRelative(-2f, -4f)
                lineToRelative(4f, -3f)
                lineToRelative(2f, 4f)
                lineToRelative(4f, 1f)
            }
            // Forward arm
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5f, 12f); lineToRelative(3f, -2f)
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbCheck",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5f, 13f); lineTo(9f, 17f); lineTo(19f, 7f)
            }
        }.build()
    }

    val GenderFemale: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbGenderFemale",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 4f)
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = 10f
                )
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = -10f
                )
                close()
                moveTo(12f, 14f); verticalLineTo(21f)
                moveTo(9f, 18f); horizontalLineTo(15f)
            }
        }.build()
    }

    val GenderMale: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbGenderMale",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10f, 14f)
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = -10f
                )
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = 10f
                )
                close()
                moveTo(14f, 10f); lineTo(20f, 4f)
                moveTo(16f, 4f); horizontalLineTo(20f); verticalLineTo(8f)
            }
        }.build()
    }

    val GenderNeutral: ImageVector by lazy {
        ImageVector.Builder(
            name = "OnbGenderNeutral",
            defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 7f)
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = 10f
                )
                arcToRelative(
                    5f,
                    5f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0f,
                    dy1 = -10f
                )
                close()
                moveTo(8f, 8f); lineToRelative(8f, 8f)
                moveTo(16f, 8f); lineToRelative(-8f, 8f)
            }
        }.build()
    }
}
