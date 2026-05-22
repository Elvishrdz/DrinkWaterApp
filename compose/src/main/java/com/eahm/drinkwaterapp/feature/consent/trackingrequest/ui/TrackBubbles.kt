package com.eahm.drinkwaterapp.feature.consent.trackingrequest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwMagentaDark

@Composable
internal fun Bubbles() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-20).dp)
                .background(Color.White.copy(alpha = 0.14f), CircleShape),
        )
        Box(
            Modifier
                .size(90.dp)
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 60.dp)
                .background(Color.White.copy(alpha = 0.09f), CircleShape),
        )
        Box(
            Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-60).dp, y = 10.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
        )
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
private fun BubblesPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFB83A95), DwMagentaDark))),
    ) {
        Bubbles()
    }
}
