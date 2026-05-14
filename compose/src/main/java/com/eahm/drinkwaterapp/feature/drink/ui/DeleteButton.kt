package com.eahm.drinkwaterapp.feature.drink.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.R
import com.eahm.drinkwaterapp.core.designsystem.icon.DwIcons
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk300

@Composable
fun DeleteButton(
    isHovered: Boolean,
    modifier: Modifier = Modifier,
) {
    val binScale by animateFloatAsState(
        targetValue = if (isHovered) 1.4f else 1f,
        animationSpec = spring(),
        label = "delete_bin_scale",
    )

    val circleSize by animateFloatAsState(
        targetValue = if (isHovered) 27.dp.value else 24.dp.value,
        animationSpec = spring(),
        label = "delete_bin_circle_size",
    )
    val gradientAlpha by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0.5f,
        animationSpec = spring(),
        label = "delete_gradient_alpha",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isHovered) Color.White else DwInk300,
        animationSpec = spring(),
        label = "delete_icon_tint",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isHovered) Color.Red else DwInk300,
        animationSpec = spring(),
        label = "delete_icon_tint",
    )

    val brush = if (isHovered) {
        Brush.radialGradient(
            colors = listOf(
                Color.Red.copy(alpha = gradientAlpha),
                Color.Transparent,
            ),
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
            ),
        )
    }

    Box(
        modifier = modifier
            .wrapContentSize()
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .background(
                brush = brush,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .padding(circleSize.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = DwIcons.Trash,
            contentDescription = stringResource(R.string.drink_delete_drop_desc),
            tint = iconTint,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = binScale
                    scaleY = binScale
                },
        )
    }
}

@Preview
@Composable
private fun NormalPreview() {
    DeleteButton(
        isHovered = false,
    )
}

@Preview
@Composable
private fun HoveredPreview() {
    DeleteButton(
        isHovered = true,
    )
}
